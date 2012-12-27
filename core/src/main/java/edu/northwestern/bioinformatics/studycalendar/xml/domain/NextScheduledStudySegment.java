/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

import java.util.Date;

/**
 * @author John Dzak
 */
public class NextScheduledStudySegment {
    Date startDate;
    StudySegment studySegment;
    NextStudySegmentMode mode;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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
