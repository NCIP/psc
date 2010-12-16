package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;

import java.util.List;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public abstract class AuditEventListenerTestCase extends DaoTestCase {
    protected final String ATTRIBUTE_NAME="ATTRIBUTE_NAME";
    protected final String PREVIOUS_VALUE="PREVIOUS_VALUE";
    protected final String NEW_VALUE="NEW_VALUE";

    protected int getEventIdForObject(int id, String className, String operation) {
      return getJdbcTemplate().queryForInt("select id from audit_events where object_id = ? and class_name = ? and operation = ?", new Object[] {id, className, operation});
    }

    protected List<Map> getAuditEventValuesForEvent(int eventId) {
        return getJdbcTemplate().queryForList("select * from audit_event_values where audit_event_id = ?", new Object[] {eventId});
    }
}
