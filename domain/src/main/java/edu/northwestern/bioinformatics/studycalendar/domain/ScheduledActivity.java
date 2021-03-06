/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_activities_id")
    }
)
public class ScheduledActivity extends AbstractMutableDomainObject implements Comparable<ScheduledActivity> {
    private ScheduledStudySegment scheduledStudySegment;
    private PlannedActivity plannedActivity;
    private Date idealDate;
    private String notes;
    private ScheduledActivityState currentState;
    private List<ScheduledActivityState> previousStates = new LinkedList<ScheduledActivityState>();
    private SortedSet<String> labels = new TreeSet<String>();
    private String details;
    private Activity activity;
    private Amendment sourceAmendment;
    private Integer repetitionNumber;

    ////// LOGIC

    public void changeState(ScheduledActivityState newState) {
        if (isChangeable()){
            if (getCurrentState() != null) {
                previousStates.add(getCurrentState());
            }
            setCurrentState(newState);
        }
    }

    public void addLabel(String label) {
        getLabels().add(label);
    }

    public void removeLabel(String label) {
        getLabels().remove(label);
    }

    public int compareTo(ScheduledActivity other) {
        if (getCurrentState() != null && other.getCurrentState() != null) {
            int timeDiff = compareTimeTo(other);
            if (timeDiff != 0) return timeDiff;
        }
        if (getPlannedActivity() != null && other.getPlannedActivity() != null) {
            Study thisStudy = getStudy(this);
            Study otherStudy = getStudy(other);

            if (thisStudy != null && otherStudy != null) {
                int studyNameDiff = ComparisonTools.nullSafeCompare(
                    thisStudy.getAssignedIdentifier(), otherStudy.getAssignedIdentifier());
                if (studyNameDiff != 0) return studyNameDiff;
            }

            int weightDiff = this.getPlannedActivity().compareWeightTo(other.getPlannedActivity());
            if (weightDiff != 0) return weightDiff;
        }

        {
            int activityDiff = getActivity().compareTo(other.getActivity());
            if (activityDiff != 0) return activityDiff;
        }

        if (getCurrentState() != null && other.getCurrentState() != null) {
            int stateDiff = getCurrentState().getMode().compareTo(other.getCurrentState().getMode());
            if (stateDiff != 0) return stateDiff;
        }

        {
            int assignmentDiff = ComparisonTools.nullSafeCompare(
                        getStudySubjectAssignmentId(this), getStudySubjectAssignmentId(other));
            if (assignmentDiff != 0) return assignmentDiff;
        }

        return ComparisonTools.nullSafeCompare(
            getId(), other.getId());
    }

    private int compareTimeTo(ScheduledActivity other) {
        if (getCurrentState().getWithTime() && other.getCurrentState().getWithTime()) {
           return getCurrentState().getDate().compareTo(other.getCurrentState().getDate());
        } else if (getCurrentState().getWithTime()) {
           return 1;
        } else if (other.getCurrentState().getWithTime()) {
           return -1;
        }
        return 0;
    }

    // this is not something I want to have as part of the public API for this class,
    // but I need to be able to invoke it on "other" SAs.
    private static Integer getStudySubjectAssignmentId(ScheduledActivity sa) {
        StudySubjectAssignment assignment = sa.getStudySubjectAssignment();
        return assignment == null ? null : assignment.getId();
    }

    @Transient
    private Study getStudy(ScheduledActivity o) {
        try {
            return o.getPlannedActivity().getPeriod().getStudySegment().getEpoch().getPlannedCalendar().getStudy();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Returns the assignment corresponding to this SA, or null if it isn't connected to one.
     * (The latter should only be the case in testing.)
     */
    @Transient
    protected StudySubjectAssignment getStudySubjectAssignment() {
        ScheduledStudySegment seg = getScheduledStudySegment();
        if (seg == null) return null;
        ScheduledCalendar cal = seg.getScheduledCalendar();
        if (cal == null) return null;
        return cal.getAssignment();
    }

    @Transient
    private boolean isChangeable() {
        Date endDate;
        if (scheduledStudySegment != null
                && scheduledStudySegment.getScheduledCalendar() != null
                && scheduledStudySegment.getScheduledCalendar().getAssignment() != null
                && scheduledStudySegment.getScheduledCalendar().getAssignment().getEndDate() != null) {
            endDate = scheduledStudySegment.getScheduledCalendar().getAssignment().getEndDate();
            return getActualDate().before(endDate);
        }
        return true;
    }

    @Transient
    public List<ScheduledActivityState> getAllStates() {
        List<ScheduledActivityState> all = new ArrayList<ScheduledActivityState>();
        if (getPreviousStates() != null) all.addAll(getPreviousStates());
        if (getCurrentState() != null) all.add(getCurrentState());
        return all;
    }

    @Transient
    public Date getActualDate() {
        Date actualDate = null;
        List<ScheduledActivityState> states = getAllStates();
        Collections.reverse(states);
        for (ScheduledActivityState state : states) {
             actualDate = state.getDate();
             if (actualDate != null) break;
        }
        
        if (actualDate == null) {
            actualDate = getIdealDate();
        }
        return actualDate;
    }
    
    @Transient
    public DayNumber getDayNumber() {
        int number = 0;
        DayNumber dayNumber;
        if(repetitionNumber == null||plannedActivity == null) {
            return null;
        }
        else {
            if(plannedActivity.getPeriod()!=null) {
                number = ((plannedActivity.getDay() + plannedActivity.getPeriod().getStartDay()-1) + ((plannedActivity.getPeriod().getDuration().getDays())*repetitionNumber));
                dayNumber = DayNumber.createCycleDayNumber(number, scheduledStudySegment.getStudySegment().getCycleLength());
                return dayNumber;
            } else {
                return null;
            }
       }
    }

    @Transient
    public boolean isOutstanding() {
        return getCurrentState().getMode().isOutstanding();
    }

    @Transient
    public boolean isConditionalEvent() {
        for (ScheduledActivityState state : getAllStates()) {
            if (state.getMode() == ScheduledActivityMode.CONDITIONAL) return true;
        }
        return false;
    }

    public void unscheduleIfOutstanding(String reason) {
        if (getCurrentState().getMode().isOutstanding()) {
            ScheduledActivityState newState
                = getCurrentState().getMode().getUnscheduleMode().createStateInstance();
            newState.setReason(reason);
            newState.setDate(getCurrentState().getDate());
            changeState(newState);
        }
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    public ScheduledStudySegment getScheduledStudySegment() {
        return scheduledStudySegment;
    }

    public void setScheduledStudySegment(ScheduledStudySegment scheduledStudySegment) {
        this.scheduledStudySegment = scheduledStudySegment;
    }

    @Fetch(FetchMode.JOIN)
    @ManyToOne
    public PlannedActivity getPlannedActivity() {
        return plannedActivity;
    }

    public void setPlannedActivity(PlannedActivity plannedActivity) {
        this.plannedActivity = plannedActivity;
    }

    @Fetch(FetchMode.JOIN)
    @Type(type = "edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate.ScheduledActivityStateType")
    @Columns(columns = {
        @Column(name = "current_state_mode_id"),
        @Column(name = "current_state_reason"),
        @Column(name = "current_state_date"),
        @Column(name = "current_state_with_time")
    })
    public ScheduledActivityState getCurrentState() {
        return currentState;
    }
    /* To implement undo, setter should have public access. Undo doesn't need to preserve the history for scheduled activity.
       But for all other uses, it is required to preserve history, so use changeState instead.
     */
    public void setCurrentState(ScheduledActivityState currentState) {
        this.currentState = currentState;
    }

    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @JoinColumn(name = "scheduled_activity_id", insertable = true, updatable = true, nullable = false)
    @Cascade({CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    @IndexColumn(name = "list_index")
    public List<ScheduledActivityState> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(List<ScheduledActivityState> previousStates) {
        this.previousStates = previousStates;
    }

    public Date getIdealDate() {
        return idealDate;
    }

    public void setIdealDate(Date idealDate) {
        this.idealDate = idealDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @ManyToOne(optional = false)
    public Amendment getSourceAmendment() {
        return sourceAmendment;
    }

    public void setSourceAmendment(Amendment sourceAmendment) {
        this.sourceAmendment = sourceAmendment;
    }

    @Fetch(FetchMode.JOIN)
    @CollectionOfElements
    @Sort(type = SortType.COMPARATOR, comparator = LabelComparator.class)
    @JoinTable(name = "scheduled_activity_labels", joinColumns = @JoinColumn(name = "scheduled_activity_id"))
    @Column(name = "label", nullable = false)
    public SortedSet<String> getLabels() {
        return labels;
    }

    public void setLabels(SortedSet<String> labels) {
        this.labels = labels;
    }

    /**
     * The repetition of the source period from which this event was created.
     * Zero-based.
     *
     * @see Period
     */
    public Integer getRepetitionNumber() {
        return repetitionNumber;
    }

    public void setRepetitionNumber(Integer repetitionNumber) {
        this.repetitionNumber = repetitionNumber;
    }

    @Transient
    public boolean isPlannedScheduledActivity() {
        return (getPlannedActivity() != null);
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
            .append("[idealDate=").append(getIdealDate());

            // Create flag for reconsent events
            if (isPlannedScheduledActivity()) {
                sb.append("; plannedActivity=").append(getPlannedActivity().getId());
            }
            sb.append("; repetition=").append(getRepetitionNumber())
              .append("; labels=").append(getLabels())
              .append(']');
        return sb.toString();
    }
}
