package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.utils.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.utils.Range;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Presenter for a subject's complete schedule across all studies.
 *
 * @author Rhett Sutphin
 */
public class SubjectCentricSchedule {
    private List<SegmentRow> segmentRows;
    private List<StudySubjectAssignment> assignments;
    private List<ScheduleDay> days;
    private MutableRange<Date> dateRange;

    public SubjectCentricSchedule(List<StudySubjectAssignment> visibleAssignments) {
        this.assignments = visibleAssignments;
        this.dateRange = new MutableRange<Date>();
        for (StudySubjectAssignment assignment : visibleAssignments) {
            for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
                if (dateRange.getStop() == null) {
                    dateRange.setFrom(segment.getDateRange());
                } else {
                    dateRange.add(segment.getDateRange());
                }
            }
        }
        buildSegmentRows();
        collectActivitiesByDay();
    }

    private void buildSegmentRows() {
        segmentRows = new LinkedList<SegmentRow>();
        for (StudySubjectAssignment assignment : assignments) {
            List<SegmentRow> assignmentRows = new LinkedList<SegmentRow>();
            for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
                SegmentRow targetRow = null;
                for (SegmentRow row : assignmentRows) {
                    if (row.willFit(segment)) {
                        targetRow = row;
                        break;
                    }
                }
                if (targetRow == null) {
                    targetRow = new SegmentRow(getDateRange(), assignmentRows.size() + segmentRows.size(), assignment);
                    assignmentRows.add(targetRow);
                }
                targetRow.add(segment);
            }
            segmentRows.addAll(assignmentRows);
        }
    }

    private void collectActivitiesByDay() {
        // Collect all days
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(dateRange.getStart());
        SimpleDateFormat keyFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, ScheduleDay> dayMap = new HashMap<String, ScheduleDay>();
        while (dayCal.getTime().compareTo(dateRange.getStop()) <= 0) {
            dayMap.put(keyFormatter.format(dayCal.getTime()), new ScheduleDay(dayCal.getTime()));
            dayCal.add(Calendar.DATE, 1);
        }

        // associate scheduled activities with days
        for (StudySubjectAssignment assignment : assignments) {
            for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
                for (ScheduledActivity activity : segment.getActivities()) {
                    ScheduleDay day = dayMap.get(keyFormatter.format(activity.getActualDate()));
                    if (day == null) {
                        throw new StudyCalendarSystemException("Scheduled study segment %s includes an activity (%s) that falls outside of the total date range for the subject.  This should not be possible.", segment, activity);
                    }
                    day.getActivities().add(activity);
                }
            }
        }

        // expose collected days as a single list
        days = new ArrayList<ScheduleDay>();
        days.addAll(dayMap.values());
        Collections.sort(days);
    }

    /////// BEAN PROPERTIES

    public List<SegmentRow> getSegmentRows() {
        return segmentRows;
    }

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public List<ScheduleDay> getDays() {
        return days;
    }
}
