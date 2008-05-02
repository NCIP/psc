package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private StudyDao studyDao;
    private DaoFinder daoFinder;
    private AmendmentService amendmentService;
    private TemplateService templateService;
    private StudyService studyService;
    private ChangeDao changeDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private DeltaService deltaService;

    public void readAndSaveTemplate(InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);

        // We do this so hibernate doesn't try to save the amendment
        study.setAmendment(null);

        // Check if development amendment is persisted and if it is, delete it
        if (study.getDevelopmentAmendment() != null) {
            String amendmentNaturalKey = study.getDevelopmentAmendment().getNaturalKey();
            Amendment dev = amendmentDao.getByNaturalKey(amendmentNaturalKey, study);
            if (dev != null) {
                amendmentService.deleteDevelopmentAmendmentOnly(study);
            }
        }

        try {
            stream.reset();
        } catch (IOException ioe) {
            throw new StudyCalendarSystemException("Problem importing template");
        }
        study = studyXmlSerializer.readDocument(stream);

        templatePostProcessing(study);
    }

    public Study readAndSaveTemplate(Study existingStudy, InputStream stream) {

        if (existingStudy != null) {
            // We do this so hibernate doesn't try to save the amendment
            existingStudy.setAmendment(null);

            // Check if development amendment is persisted and if it is, delete it
            if (existingStudy.getDevelopmentAmendment() != null) {
                String amendmentNaturalKey = existingStudy.getDevelopmentAmendment().getNaturalKey();
                Amendment dev = amendmentDao.getByNaturalKey(amendmentNaturalKey, existingStudy);
                if (dev != null) {
                    amendmentService.deleteDevelopmentAmendmentOnly(existingStudy);
                }
            }
        }

        Study study = studyXmlSerializer.readDocument(stream);

        templatePostProcessing(study);
        return study;
    }

    /**
     * @param study study that needs to be created or imported
     */
    public void templatePostProcessing(Study study) {
        resolveExistingActivitiesAndSources(study);
        resolveChangeChildrenFromPlanTreeNodeTree(study);
    }

    protected void resolveExistingActivitiesAndSources(Study study) {
        List<PlannedActivity> all = new LinkedList<PlannedActivity>();


        List<Amendment> reverse = new LinkedList<Amendment>(study.getAmendmentsList());
        Collections.reverse(reverse);
        if (study.getDevelopmentAmendment() != null) {
            reverse.add(study.getDevelopmentAmendment());
        }
        for (Amendment amendment : reverse) {
            for (Delta<?> delta : amendment.getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change.getAction() == ChangeAction.ADD) {
                        PlanTreeNode<?> child = ((Add) change).getChild();
                        // Need this if change is already persisted in the database, then it won't have a child
                        // and we need to find from the child id
                        if (child == null) {
                            Class childClass = ((PlanTreeInnerNode) delta.getNode()).childClass();
                            DomainObjectDao dao = daoFinder.findDao(childClass);
                            child = (PlanTreeNode<?>) dao.getById(((ChildrenChange)change).getChildId());
                        }
                        
                        if (child instanceof PlannedActivity) {
                            all.add((PlannedActivity) child);
                        } else {
                            all.addAll(templateService.findChildren((PlanTreeInnerNode) child, PlannedActivity.class));
                        }
                    }
                }
            }
        }
        Amendment dev = study.getDevelopmentAmendment();
        Amendment cur = study.getAmendment();
        study.setAmendment(null);
        study.setDevelopmentAmendment(null);
        for (PlannedActivity plannedActivity : all) {
            Activity activity = plannedActivity.getActivity();

            Activity existingActivity = activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getName());
            if (existingActivity != null) {
                plannedActivity.setActivity(existingActivity);
            } else {
                Source existingSource = sourceDao.getByName(activity.getSource().getName());
                if (existingSource != null) {
                    activity.setSource(existingSource);
                }
            }

            sourceDao.save(plannedActivity.getActivity().getSource());
            activityDao.save(plannedActivity.getActivity());
        }
        study.setAmendment(cur);
        study.setDevelopmentAmendment(dev);
    }

    protected void resolveChangeChildrenFromPlanTreeNodeTree(Study study) {
        List<Amendment> amendments = new ArrayList<Amendment>(study.getAmendmentsList());
        Amendment development = study.getDevelopmentAmendment();
        Collections.reverse(amendments);
        study.setAmendment(null);
        study.setDevelopmentAmendment(null);

        // StudyDao is being used instead of StudyService because we don't want to cascade to the amendments yet
        studyDao.save(study);

        for (Amendment amendment : amendments) {
            // If amendment already exists, we don't want to amend the study with it twice.
            if (amendment.getId() == null) {
                for (Delta delta : amendment.getDeltas()) {
                    // resolve node
                    PlanTreeNode<?> deltaNode = findRealNode(delta.getNode());
                    if (deltaNode != null) {
                        delta.setNode(deltaNode);
                    } else {
                        throw new IllegalStateException("Delta " + delta.getGridId() + " references unknown node " + delta.getNode().getGridId());
                    }

                    // resolve child nodes
                    for (Object oChange : delta.getChanges()) {
                        if (oChange instanceof ChildrenChange) {
                            ChildrenChange change = (ChildrenChange) oChange;
                            PlanTreeNode<?> nodeTemplate = change.getChild();

                            // If node template is not null, element has yet to be persisted
                            if (nodeTemplate != null) {
                                PlanTreeNode<?> node = findRealNode(nodeTemplate);
                                if (node != null) {
                                    change.setChild(node);
                                }
                            }
                        }
                    }
                }
                study.setDevelopmentAmendment(amendment);
                study.setAmendment(amendment.getPreviousAmendment());
                amendmentService.amend(study);
            }
        }

        // Resolve delta nodes and child nodes for development amendment
        if (development != null) {
            for (Delta delta : development.getDeltas()) {
                // resolve node
                PlanTreeNode<?> deltaNode = findRealNode(delta.getNode());
                if (deltaNode != null) {
                    delta.setNode(deltaNode);
                } else {
                    throw new IllegalStateException("Delta " + delta.getGridId() + " references unknown node " + delta.getNode().getGridId());
                }

                // resolve child nodes
                for (Object oChange : delta.getChanges()) {
                    if (oChange instanceof ChildrenChange) {
                        ChildrenChange change = (ChildrenChange) oChange;
                        PlanTreeNode<?> nodeTemplate = change.getChild();
                        PlanTreeNode<?> node = findRealNode(nodeTemplate);
                        if (node != null) {
                            change.setChild(node);
                        }
                    }
                }
            }
        }
        study.setDevelopmentAmendment(development);
        studyService.save(study);

    }

    private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
        GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
        return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
    }

    ////// Bean Setters

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }

    @Required
    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
