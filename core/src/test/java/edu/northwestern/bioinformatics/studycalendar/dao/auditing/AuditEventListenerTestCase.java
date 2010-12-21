package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Jalpa Patel
 */
public abstract class AuditEventListenerTestCase extends DaoTestCase {

    protected int getEventIdForObject(int id, String className, String operation) {
      return getJdbcTemplate().queryForInt("select id from audit_events where object_id = ? and class_name = ? and operation = ?", new Object[] {id, className, operation});
    }

    protected DataAuditEventValue getAuditEventValueFor(int eventId, String attributeName) {
        return (DataAuditEventValue) getJdbcTemplate().query(
                "select attribute_name, previous_value, new_value from audit_event_values where audit_event_id=? and attribute_name=?",
                new Object[] {eventId, attributeName},
                new DataAuditEventValueRecordResultSetExtractor());
    }

    private class DataAuditEventValueRecordResultSetExtractor implements ResultSetExtractor {
        public DataAuditEventValue extractData(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return new DataAuditEventValue(rs.getString("attribute_name"), rs.getString("previous_value"), rs.getString("new_value"));
            } else {
                return null;
            }
        }

    }
}
