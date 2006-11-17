package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Rhett Sutphin
 */
public interface PeriodCommand {
    Arm getArm();
    void apply();
}
