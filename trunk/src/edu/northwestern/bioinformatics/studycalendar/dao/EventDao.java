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
public class EventDao extends HibernateDaoSupport {
    private final Log log = LogFactory.getLog(getClass());

    public PlannedEvent getPlannedEventById(int id) {
        return (PlannedEvent) getHibernateTemplate().get(PlannedEvent.class, id);
    }
}
