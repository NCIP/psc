package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

/**
 * @author Rhett Sutphin
 */
public interface PeriodCommand {
    StudySegment getStudySegment();
    void apply();
}
