package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private StudyDao studyDao;
    private DaoFinder daoFinder;
    private AmendmentService amendmentService;
    private TemplateService templateService;

    public void readAndSaveTemplate(InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);
        importTemplate(study);
    }

    public void importTemplate(Study study) {
        resolveExistingActivitiesAndSources(study);
        resolveChangeChildrenFromPlanTreeNodeTree(study);
    }

    protected void resolveExistingActivitiesAndSources(Study study) {
        List<PlannedActivity> all = new LinkedList<PlannedActivity>();

        List<Amendment> reverse = new LinkedList<Amendment>(study.getAmendmentsList());
        Collections.reverse(reverse);
        for (Amendment amendment : reverse) {
            for (Delta<?> delta : amendment.getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change.getAction() == ChangeAction.ADD) {
                        PlanTreeNode<?> child = ((Add) change).getChild();
                        if (child instanceof PlannedActivity) {
                            all.add((PlannedActivity) child);
                        } else {
                            all.addAll(templateService.findChildren((PlanTreeInnerNode) child, PlannedActivity.class));
                        }
                    }
                }
            }
        }

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
    }

    protected void resolveChangeChildrenFromPlanTreeNodeTree(Study study) {
        List<Amendment> amendments = new ArrayList<Amendment>(study.getAmendmentsList());
        Collections.reverse(amendments);
        study.setAmendment(null);

        // StudyDao is being used instead of StudyService because we don't want to cascade to the amendments yet
        studyDao.save(study);

        for (Amendment amendment : amendments) {
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
                        PlanTreeNode<?> node = findRealNode(nodeTemplate);
                        if (node != null) {
                            change.setChild(node);
                        }
                    }
                }
            }
            study.setDevelopmentAmendment(amendment);
            amendmentService.amend(study);
        }
    }

    private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
        GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
        return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
    }


    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
