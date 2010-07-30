package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.tools.Range;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of non-overlapping study segments from a single study for a particular assignment.
 *
 * @author Rhett Sutphin
 */
public class SegmentRow {
    private Range<Date> dateRange;
    private int rowNumber;
    private List<ScheduledStudySegment> segments;
    private StudySubjectAssignment assignment;

    public SegmentRow(Date startDate, Date endDate, int rowNumber, StudySubjectAssignment assignment) {
        this(new Range<Date>(startDate, endDate), rowNumber, assignment);
    }

    public SegmentRow(Range<Date> dateRange, int rowNumber, StudySubjectAssignment assignment) {
        this.dateRange = dateRange;
        this.rowNumber = rowNumber;
        this.assignment = assignment;
        segments = new LinkedList<ScheduledStudySegment>();
    }

    public boolean willFit(ScheduledStudySegment candidate) {
        Range<Date> candidateRange = candidate.getDateRange();
        if (!dateRange.includes(candidateRange)) return false;

        for (ScheduledStudySegment segment : segments) {
            if (segment.getDateRange().intersects(candidateRange)) {
                return false;
            }
        }
        return true;
    }

    public void add(ScheduledStudySegment scheduledStudySegment) {
        if (!willFit(scheduledStudySegment)) {
            throw new StudyCalendarSystemException("%s will not fit in this row.  Use willFit to check first.", scheduledStudySegment);
        }
        segments.add(scheduledStudySegment);
    }

    ////// PROPERTIES

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public List<ScheduledStudySegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public StudySubjectAssignment getAssignment() {
        return assignment;
    }
}
