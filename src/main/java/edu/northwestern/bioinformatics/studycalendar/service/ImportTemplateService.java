package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.TemplateXmlSerializerPostProcessor;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private StudyDao studyDao;
    private DaoFinder daoFinder;
    private PlannedActivityDao plannedActivityDao;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;
    private EpochDao epochDao;
    private ChangeDao changeDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private TemplateXmlSerializerPostProcessor templatePostProcessor;

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
        templatePostProcessor.resolveExistingActivitiesAndSources(study);
        templatePostProcessor.resolveChangeChildrenFromPlanTreeNodeTree(study);
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
    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
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

    @Required
    public void setTemplateXmlPostProcessor(TemplateXmlSerializerPostProcessor templatePostProcessor) {
        this.templatePostProcessor = templatePostProcessor;
    }
}
