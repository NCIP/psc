package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.tools.Range;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Presenter for a single linear view across multiple schedules.
 *
 * @author Rhett Sutphin
 */
public class MultipleAssignmentScheduleView {
    private List<SegmentRow> segmentRows;
    private List<StudySubjectAssignment> visibleAssignments;
    private List<StudySubjectAssignment> hiddenAssignments;
    private List<ScheduleDay> days;
    private MutableRange<Date> dateRange;
    private Map<String, Date> datesImmediatePerProtocol;
    private List<Study> studies;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private NowFactory nowFactory;
    private final SimpleDateFormat dayMapKeyFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public MultipleAssignmentScheduleView(List<StudySubjectAssignment> visibleAssignments, List<StudySubjectAssignment> hiddenAssignments, NowFactory nowFactory) {
        this.visibleAssignments = visibleAssignments;
        this.hiddenAssignments = hiddenAssignments;
        this.nowFactory = nowFactory;
        List<StudySubjectAssignment> allAssignments = new ArrayList<StudySubjectAssignment>(visibleAssignments.size() + hiddenAssignments.size());
        allAssignments.addAll(visibleAssignments); allAssignments.addAll(hiddenAssignments);
        this.dateRange = new MutableRange<Date>();
        this.datesImmediatePerProtocol = new HashMap<String, Date>();
        this.studies = new ArrayList<Study>();
        
        for (StudySubjectAssignment assignment : allAssignments) {
            for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
                if (dateRange.getStop() == null) {
                    dateRange.setFrom(segment.getDateRange());
                } else {
                    dateRange.add(segment.getDateRange());
                }
            }
            studies.add(assignment.getStudySite().getStudy());
            datesImmediatePerProtocol = createDates(assignment.getScheduledCalendar());
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

    //Keeping the method for the case if we'd later decide to reschedule the studySegment and not the full study
    private Map<String, Date> createDates(ScheduledCalendar scheduledCalendar) {
         Map<String, Date> dates = new HashMap<String, Date>();

         Date perProtocolDate = null;
         List<ScheduledStudySegment> existingStudySegments = scheduledCalendar.getScheduledStudySegments();
         if (existingStudySegments.size() > 0) {
             ScheduledStudySegment lastStudySegment = existingStudySegments.get(existingStudySegments.size() - 1);
             log.debug("Building PER_PROTOCOL start date from " + lastStudySegment);
             perProtocolDate = lastStudySegment.getNextStudySegmentPerProtocolStartDate();
         }
         dates.put(NextStudySegmentMode.PER_PROTOCOL.name(), perProtocolDate);
         dates.put(NextStudySegmentMode.IMMEDIATE.name(), new Date());
         return dates;
     }
    

    ////// LOGIC

    public boolean getIncludesToday() {
        return getDateRange().includes(nowFactory.getNow());
    }

    ////// BEAN PROPERTIES

    public List<SegmentRow> getSegmentRows() {
        return segmentRows;
    }

    public List<StudySubjectAssignment> getVisibleAssignments() {
        return visibleAssignments;
    }

    public List<StudySubjectAssignment> getHiddenAssignments() {
        return hiddenAssignments;
    }

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public List<Study> getStudies() {
        return studies;
    }

    public Map<String, Date> getDatesImmediatePerProtocol() {
        return datesImmediatePerProtocol;
    }

    public List<ScheduleDay> getDays() {
        return days;
    }

    private interface ScheduledActivityAction {
        void yield(ScheduledActivity activity, ScheduleDay day);
    }
}
