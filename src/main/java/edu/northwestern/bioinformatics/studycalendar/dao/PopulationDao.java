package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;

/**
 * @author Rhett Sutphin
 */
public class PopulationDao extends StudyCalendarMutableDomainObjectDao<Population> {
    public Class<Population> domainClass() {
        return Population.class;
    }
}
