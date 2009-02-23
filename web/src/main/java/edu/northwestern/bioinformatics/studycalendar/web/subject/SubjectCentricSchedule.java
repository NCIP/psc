package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.tools.Range;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import gov.nih.nci.cabig.ctms.lang.NowFactory;

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
    private List<StudySubjectAssignment> visibleAssignments;
    private List<StudySubjectAssignment> hiddenAssignments;
    private List<ScheduleDay> days;
    private MutableRange<Date> dateRange;

    private NowFactory nowFactory;
    private final SimpleDateFormat dayMapKeyFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public SubjectCentricSchedule(List<StudySubjectAssignment> visibleAssignments, List<StudySubjectAssignment> hiddenAssignments, NowFactory nowFactory) {
        this.visibleAssignments = visibleAssignments;
        this.hiddenAssignments = hiddenAssignments;
        this.nowFactory = nowFactory;
        List<StudySubjectAssignment> allAssignments = new ArrayList<StudySubjectAssignment>(visibleAssignments.size() + hiddenAssignments.size());
        allAssignments.addAll(visibleAssignments); allAssignments.addAll(hiddenAssignments);
        this.dateRange = new MutableRange<Date>();
        for (StudySubjectAssignment assignment : allAssignments) {
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

    ////// INITIALIZATION

    private void buildSegmentRows() {
        segmentRows = new LinkedList<SegmentRow>();
        for (StudySubjectAssignment assignment : visibleAssignments) {
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
        Map<String, ScheduleDay> dayMap = createAllScheduleDays();

        associateVisibleScheduledActivitiesWithDays(dayMap);
        associateHiddenScheduledActivitiesWithDays(dayMap);

        // expose collected days as a single list
        days = new ArrayList<ScheduleDay>();
        days.addAll(dayMap.values());
        Collections.sort(days);
    }

    private Map<String, ScheduleDay> createAllScheduleDays() {
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(dateRange.getStart());
        Map<String, ScheduleDay> dayMap = new HashMap<String, ScheduleDay>();
        while (dayCal.getTime().compareTo(dateRange.getStop()) <= 0) {
            dayMap.put(dayMapKeyFormatter.format(dayCal.getTime()), new ScheduleDay(dayCal.getTime()));
            dayCal.add(Calendar.DATE, 1);
        }
        return dayMap;
    }

    private void associateVisibleScheduledActivitiesWithDays(Map<String, ScheduleDay> dayMap) {
        forEachScheduledActivity(visibleAssignments, dayMap, new ScheduledActivityAction() {
            public void yield(ScheduledActivity activity, ScheduleDay day) {
                day.getActivities().add(activity);
            }
        });
    }

    private void associateHiddenScheduledActivitiesWithDays(Map<String, ScheduleDay> dayMap) {
        forEachScheduledActivity(hiddenAssignments, dayMap, new ScheduledActivityAction() {
            public void yield(ScheduledActivity activity, ScheduleDay day) {
                day.setHasHiddenActivities(true);
            }
        });
    }

    private void forEachScheduledActivity(List<StudySubjectAssignment> assignments, Map<String, ScheduleDay> dayMap, ScheduledActivityAction action) {
        for (StudySubjectAssignment assignment : assignments) {
            for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
                for (ScheduledActivity activity : segment.getActivities()) {
                    ScheduleDay day = dayMap.get(dayMapKeyFormatter.format(activity.getActualDate()));
                    if (day == null) {
                        throw new StudyCalendarSystemException("Scheduled study segment %s includes an activity (%s) that falls outside of the total date range for the subject.  This should not be possible.", segment, activity);
                    }
                    action.yield(activity, day);
                }
            }
        }
    }

    ////// LOGIC

    public boolean getIncludesToday() {
        return getDateRange().includes(nowFactory.getNow());
    }

    ////// BEAN PROPERTIES

    public List<SegmentRow> getSegmentRows() {
        return segmentRows;
    }

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public List<ScheduleDay> getDays() {
        return days;
    }

    private interface ScheduledActivityAction {
        void yield(ScheduledActivity activity, ScheduleDay day);
    }
}
