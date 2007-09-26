package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;

/**
 * @author Rhett Sutphin
 */
public class ScheduledArmDao extends StudyCalendarDao<ScheduledArm> {
    public Class<ScheduledArm> domainClass() {
        return ScheduledArm.class;
    }
}
