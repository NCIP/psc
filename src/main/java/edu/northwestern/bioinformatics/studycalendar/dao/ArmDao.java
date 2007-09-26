package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class ArmDao extends StudyCalendarMutableDomainObjectDao<Arm> {
    @Override
    public Class<Arm> domainClass() {
        return Arm.class;
    }
}
