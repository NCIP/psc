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
    private PlannedActivityDao plannedActivityDao;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;
    private EpochDao epochDao;
    private ChangeDao changeDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;

    public void readAndSaveTemplate(InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);

        deleteDevelopmentAmendment(study);

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
            deleteDevelopmentAmendment(existingStudy);
        }

        Study study = studyXmlSerializer.readDocument(stream);

        templatePostProcessing(study);
        return study;
    }

    /**
     * This is needed because since the development amendment is changeable, we don't
     * want to have to merge the changes with a newer version.  So our solution is to delete
     * it and then de-serialize again.
     * 
     * @param existingStudy
     */
    private void deleteDevelopmentAmendment(Study existingStudy) {
        // We do this so hibernate doesn't try to save the amendment
        existingStudy.setAmendment(null);

        // Check if development amendment is persisted and if it is, delete it
        if (existingStudy.getDevelopmentAmendment() != null) {
            String amendmentNaturalKey = existingStudy.getDevelopmentAmendment().getNaturalKey();
            Amendment dev = amendmentDao.getByNaturalKey(amendmentNaturalKey);
            if (dev != null) {
                deleteAmendment(existingStudy.getDevelopmentAmendment());
                existingStudy.setDevelopmentAmendment(null);
                studyDao.save(existingStudy);
            }
        }
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
                            child = findChangeChild(change);
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
                resolveDeltaNodesAndChangeChildren(amendment);
                
                study.setDevelopmentAmendment(amendment);
                study.setAmendment(amendment.getPreviousAmendment());
                amendmentService.amend(study);
            }
        }

        // Resolve delta nodes and child nodes for development amendment
        if (development != null) {
            resolveDeltaNodesAndChangeChildren(development);
        }
        study.setDevelopmentAmendment(development);
        studyService.save(study);

    }

    private void resolveDeltaNodesAndChangeChildren(Amendment amendment) {
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

                    // If nodeTemplate is not null, element has yet to be persisted (because we setChild in serializer).
                    // So since we have a template for the node, we want to find the actual node.
                    if (nodeTemplate != null) {
                        PlanTreeNode<?> node = findRealNode(nodeTemplate);
                        if (node != null) {
                            change.setChild(node);
                        }
                    }
                }
            }
        }
    }

    /**
     * When the study gets de-serialized, references to new PlanTreeNode(s) cannot be found since the whole
     * template isn't saved until the end. Therefore, after the template is de-serialized, we have to go
     * and resolve the PlanTreeNode(s) to their correct references.
     *
     * e.g. A Template that has two amendments is being imported.  The first amendment adds a new Epoch. The second
     *      amendment changes the name of that Epoch.  When the second amendment is de-serialized, the Delta node will have
     *      a template Epoch (an epoch with the same grid id as the actual one) as a place holder for the actual Epoch
     *      until it is resolved.
     */
    private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
        GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
        return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
    }

    /**
     * When a change is persisted to the database, when retreived the child element is null.  We must retrieve it
     * using the child id.
     */
    private PlanTreeNode<?> findChangeChild(Change change) {
        Integer childId = ((ChildrenChange)change).getChildId();
        Class<? extends PlanTreeNode> childClass = ((PlanTreeInnerNode) change.getDelta().getNode()).childClass();

        DomainObjectDao dao = daoFinder.findDao(childClass);
        PlanTreeNode<?> child = (PlanTreeNode<?>) dao.getById(childId);
        if (child == null) {
            throw new StudyCalendarSystemException("Problem importing template. Child with class %s and id %s could not be found",
                    childClass.getName(), childId.toString());
        }
        return child;
    }

    public void deleteAmendment(Amendment amendment) {
        amendmentDao.delete(amendment);
        deleteDeltas(amendment.getDeltas());
    }

    protected void deleteDeltas (List<Delta<?>> deltas) {
        for (Delta delta : deltas) {
            deltaDao.delete(delta);


            List<Change> changesToRemove = new ArrayList<Change>();
            for (Object oChange : delta.getChanges()) {
                Change change = (Change) oChange;
                changesToRemove.add(change);
                changeDao.delete(change);
                if (ChangeAction.ADD.equals(change.getAction())) {
                    PlanTreeNode<?> child = findChangeChild(change);

                    if (child instanceof Epoch) {
                        deleteEpoch((Epoch) child);
                    } else if (child instanceof StudySegment) {
                        deleteStudySegment((StudySegment) child);
                    } else if (child instanceof Period) {
                        deletePeriod((Period) child);
                    } else if (child instanceof PlannedActivity) {
                        deletePlannedActivity((PlannedActivity) child);
                    }
                }

            }
            for (Change change : changesToRemove) {
                delta.removeChange(change);
            }
        }
        deltas.clear();
    }

    // We don't want to add delete-orphaned to the hibernate cascade because
    // if planned tree node is part of another amendment, we don't want to destroy
    // that association also.
    protected void deletePlannedCalendar(PlannedCalendar calendar) {
        deleteEpochs(calendar.getEpochs());
    }

    protected void deleteEpochs(List<Epoch> epochs) {
        for (Epoch epoch : epochs) {
            deleteEpoch(epoch);
        }
        epochs.clear();
    }

    protected void deleteEpoch(Epoch epoch) {
        deleteStudySegments(epoch.getStudySegments());
        epochDao.delete(epoch);
    }

    protected void deleteStudySegments(List<StudySegment> segments) {
        for (StudySegment segment : segments) {
            deleteStudySegment(segment);
        }
        segments.clear();
    }

    private void deleteStudySegment(StudySegment segment) {
        deletePeriods(segment.getPeriods());
        studySegmentDao.delete(segment);
    }

    protected void deletePeriods(Set<Period> periods) {
        for(Period period : periods) {
            deletePeriod(period);
        }
        periods.clear();
    }

    private void deletePeriod(Period period) {
        deletePlannedActivities(period.getPlannedActivities());
        periodDao.delete(period);
    }

    protected void deletePlannedActivities(List<PlannedActivity> plannedActivities) {
        for (PlannedActivity activity : plannedActivities) {
            deletePlannedActivity(activity);
        }
        plannedActivities.clear();
    }

    private void deletePlannedActivity(PlannedActivity activity) {
        plannedActivityDao.delete(activity);
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
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
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
}
