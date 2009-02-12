package edu.northwestern.bioinformatics.studycalendar.xml.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

import java.util.Date;

/**
 * @author John Dzak
 */
public class NextScheduledStudySegment {
    Date startDate;
    Integer startDay;
    StudySegment studySegment;
    NextStudySegmentMode mode;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }

    public NextStudySegmentMode getMode() {
        return mode;
    }

    public void setMode(NextStudySegmentMode mode) {
        this.mode = mode;
    }
}
