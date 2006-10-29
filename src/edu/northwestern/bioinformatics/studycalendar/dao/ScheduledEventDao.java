package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventDao extends StudyCalendarDao<ScheduledEvent> {
    public Class<ScheduledEvent> domainClass() { return ScheduledEvent.class; }
}
