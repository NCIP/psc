/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.tools.Range;
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
 * Presenter for a single linear view across multiple schedules.
 *
 * @author Rhett Sutphin
 */
public class MultipleAssignmentScheduleView {
    private final SimpleDateFormat dayMapKeyFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private List<SegmentRow> segmentRows;
    private List<ScheduleDay> days;
    private MutableRange<Date> dateRange;
    private List<StudySubjectAssignment> visibleAssignments;
    private List<UserStudySubjectAssignmentRelationship> relatedAssignments;

    private NowFactory nowFactory;

    public MultipleAssignmentScheduleView(List<UserStudySubjectAssignmentRelationship> relatedAssignments, NowFactory nowFactory) {
        this.relatedAssignments = relatedAssignments;
        this.nowFactory = nowFactory;
        this.dateRange = new MutableRange<Date>();
        this.visibleAssignments = new ArrayList<StudySubjectAssignment>(relatedAssignments.size());

        for (UserStudySubjectAssignmentRelationship related : relatedAssignments) {
            for (ScheduledStudySegment segment : related.getAssignment().getScheduledCalendar().getScheduledStudySegments()) {
                if (dateRange.getStop() == null) {
                    dateRange.setFrom(segment.getDateRange());
                } else {
                    dateRange.add(segment.getDateRange());
                }
            }
            if (related.isVisible()) visibleAssignments.add(related.getAssignment());
        }
        buildSegmentRows();
        collectActivitiesByDay();
    }

    ////// INITIALIZATION

    private void buildSegmentRows() {
        segmentRows = new LinkedList<SegmentRow>();
        for (UserStudySubjectAssignmentRelationship related : relatedAssignments) {
            if (!related.isVisible()) continue;
            List<SegmentRow> assignmentRows = new LinkedList<SegmentRow>();
            for (ScheduledStudySegment segment : related.getAssignment().getScheduledCalendar().getScheduledStudySegments()) {
                SegmentRow targetRow = null;
                for (SegmentRow row : assignmentRows) {
                    if (row.willFit(segment)) {
                        targetRow = row;
                        break;
                    }
                }
                if (targetRow == null) {
                    targetRow = new SegmentRow(getDateRange(),
                        assignmentRows.size() + segmentRows.size(), related.getAssignment());
                    assignmentRows.add(targetRow);
                }
                targetRow.add(segment);
            }
            segmentRows.addAll(assignmentRows);
        }
    }

    private void collectActivitiesByDay() {
        Map<String, ScheduleDay> dayMap = createAllScheduleDays();

        associateScheduledActivitiesWithDays(dayMap);

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

    private void associateScheduledActivitiesWithDays(Map<String, ScheduleDay> dayMap) {
        for (UserStudySubjectAssignmentRelationship related : this.relatedAssignments) {
            for (ScheduledStudySegment segment : related.getAssignment().getScheduledCalendar().getScheduledStudySegments()) {
                for (ScheduledActivity activity : segment.getActivities()) {
                    ScheduleDay day = dayMap.get(dayMapKeyFormatter.format(activity.getActualDate()));
                    if (day == null) {
                        throw new StudyCalendarSystemException("Scheduled study segment %s includes an activity (%s) that falls outside of the total date range for the subject.  This should not be possible.", segment, activity);
                    }
                    if (related.isVisible()) {
                        day.getActivities().add(activity);
                        // TODO: discriminate between editable and non-editable activities
                    } else {
                        day.setHasHiddenActivities(true);
                    }
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

    public List<StudySubjectAssignment> getVisibleAssignments() {
        return visibleAssignments;
    }

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public List<ScheduleDay> getDays() {
        return days;
    }
}
