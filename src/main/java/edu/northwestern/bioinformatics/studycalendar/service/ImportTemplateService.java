package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import org.springframework.transaction.annotation.Transactional;

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

    public void readAndSaveTemplate(InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);
        importTemplate(study);
    }

    /**
     * @param study study that needs to be created or imported
     */
    public void importTemplate(Study study) {
        resolveExistingActivitiesAndSources(study);
        resolveChangeChildrenFromPlanTreeNodeTree(study);
    }

    /**
     * Creates or updates the study
     * <p> Creates a new study if study does not exists, i.e. existingTemplate parameter is null
     * <p>Or if study already exists, updates the study by merging the new template with the existing one using these rules..
     * <li>Do not change any existing released amendments </li>
     * <li>Import any new released amendments (automatically creating activities, etc.)</li>
     * <li>Update the existing development amendment with any changes in the new one  </li>
     * </p>
     *
     * @param existingTemplate study which already exists and which needs to merged from new study 
     * @param newTemplate      new study
     */
    public void mergeTemplate(final Study existingTemplate, final Study newTemplate) {
        if (existingTemplate == null) {
            importTemplate(newTemplate);
        }//FIXME:Saurabh: implement the logic of merging two templates

    }

    protected void resolveExistingActivitiesAndSources(Study study) {
        List<PlannedActivity> all = new LinkedList<PlannedActivity>();


        List<Amendment> reverse = new LinkedList<Amendment>(study.getAmendmentsList());
        Collections.reverse(reverse);
        reverse.add(study.getDevelopmentAmendment());
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
        Amendment development = study.getDevelopmentAmendment();
        Collections.reverse(amendments);
        study.setAmendment(null);
        study.setDevelopmentAmendment(null);

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

        // Resolve delta nodes and child nodes for development amendment
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
        study.setDevelopmentAmendment(development);
        studyService.save(study);

    }

    private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
        GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
        return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
    }

    // We don't want to add delete-orphaned to the hibernate cascade because
    // if planned tree node is part of another amendment, we don't want to destroy
    // that association also.
    protected void deletePlannedCalendar(PlannedCalendar calendar) {
        deleteEpochs(calendar.getEpochs());
    }

    protected void deleteEpochs(List<Epoch> epochs) {
        for (Epoch epoch : epochs) {
            deleteStudySegments(epoch.getStudySegments());
            epochDao.delete(epoch);
        }
        epochs.clear();
    }

    protected void deleteStudySegments(List<StudySegment> segments) {
        for (StudySegment segment : segments) {
            deletePeriods(segment.getPeriods());
            studySegmentDao.delete(segment);
        }
        segments.clear();
    }

    protected void deletePeriods(Set<Period> periods) {
        for(Period period : periods) {
            deletePlannedActivities(period.getPlannedActivities());
            periodDao.delete(period);
        }
        periods.clear();
    }

    protected void deletePlannedActivities(List<PlannedActivity> plannedActivities) {
        for (PlannedActivity activity : plannedActivities) {
            plannedActivityDao.delete(activity);
        }
        plannedActivities.clear();
    }

    ////// Bean Setters
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

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }
}
