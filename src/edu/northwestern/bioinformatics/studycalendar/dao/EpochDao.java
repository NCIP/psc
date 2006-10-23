package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

/**
 * @author Rhett Sutphin
 */
public class EpochDao extends StudyCalendarDao<Epoch> {
    public Class<Epoch> domainClass() { return Epoch.class; }
}
