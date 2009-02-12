package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

/**
 * @author Rhett Sutphin
 */
public interface PeriodCommand {
    Period getPeriod();
    StudySegment getStudySegment();

    /**
     * @return true to redirect back to the edit period page on completion; false to go to the template.
     */
    boolean apply();
}
