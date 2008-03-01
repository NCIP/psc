package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.xml.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author John Dzak
 */
public class StudyXmlSerializerPreProcessor {
    private StudyDao studyDao;
    private PlannedActivityDao plannedActivityDao;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;
    private EpochDao epochDao;
    private ChangeDao changeDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private XmlUtils xmlUtils;

    public void process(Study study) {
        deleteDevelopmentAmendment(study);
    }

    /**
     * This is needed because since the development amendment is changeable, we don't
     * want to have to merge the changes with a newer version.  So our solution is to delete
     * it and then de-serialize the development amendment again.
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
                    PlanTreeNode<?> child = xmlUtils.findChangeChild(change);

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

    /////// Bean Setters

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
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

    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }

    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    public void setXmlUtils(XmlUtils xmlUtils) {
        this.xmlUtils = xmlUtils;
    }
}
