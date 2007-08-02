package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.metadata.ClassMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

import java.util.Map;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class PlannedEventDao extends StudyCalendarDao<PlannedEvent> {
    public Class<PlannedEvent> domainClass() {
        return PlannedEvent.class;
    }
}
