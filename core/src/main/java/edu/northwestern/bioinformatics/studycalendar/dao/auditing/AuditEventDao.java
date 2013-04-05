/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang.StringUtils.join;

/**
 * @author Jalpa Patel
 */
@Transactional
public class AuditEventDao implements InitializingBean {
    private JdbcTemplate jdbcTemplate;
    private String databaseType;

    public void saveEvent(AuditEvent event) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate.getDataSource());
        insert.withTableName("audit_events");
        Map<String, Object> parameters =  new HashMap<String, Object>();
        final DataAuditInfo info = event.getInfo();
        final DataReference reference = event.getReference();
        parameters.put("ip_address", info.getIp());
        parameters.put("user_name", info.getUsername());
        parameters.put("time", info.getTime());
        parameters.put("class_name", reference.getClassName());
        parameters.put("operation", event.getOperation().toString());
        parameters.put("url", info.getUrl());
        /*  Note: version is hardcoded to 0 otherwise SQLException is thrown for non-nullable column,
            if not included in SimpleJdbcInsert query's parameters.
         */
        parameters.put("version", 0);
        parameters.put("object_id", reference.getId());
        parameters.put("user_action_id", event.getUserActionId());

        if (databaseType.contains("Oracle")){
            int eventId = jdbcTemplate.queryForInt("SELECT SEQ_AUDIT_EVENTS_ID.nextval FROM dual");
            parameters.put("id", eventId);
            insert.execute(parameters);
            final String updateStatement = "insert into audit_event_values(id, audit_event_id, attribute_name , previous_value, new_value) values (?,?,?,?,?)";
            for (DataAuditEventValue value : event.getValues()) {
                int valueId = jdbcTemplate.queryForInt("SELECT SEQ_AUDIT_EVENT_VALUES_ID.nextval FROM dual");
                jdbcTemplate.update(updateStatement, new Object[] {valueId, eventId, value.getAttributeName(), value.getPreviousValue(), value.getCurrentValue()});
            } 
        } else {
            int eventId = insert.usingGeneratedKeyColumns("id").executeAndReturnKey(parameters).intValue();
            final String updateStatement = "insert into audit_event_values(audit_event_id, attribute_name , previous_value, new_value) values (?,?,?,?)";
            for (DataAuditEventValue value : event.getValues()) {
                jdbcTemplate.update(updateStatement, new Object[] {eventId, value.getAttributeName(), value.getPreviousValue(), value.getCurrentValue()});
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        setDatabaseType(determineDatabaseType());
    }

    @SuppressWarnings("unchecked")
    public List<AuditEvent> getAuditEventsByUserActionId(String userActionId) {
        String[] query =  new String[] {
                "select ip_address, user_name, time, class_name, operation, url, object_id, user_action_id",
                "from Audit_Events where user_action_id = ?"
        };
        List<AuditEvent> events =  jdbcTemplate.query(join(query, ' '), new Object[] {userActionId}, new AuditEventRowMappper(false));
        Collections.sort(events);
        return events;
    }

    @SuppressWarnings("unchecked")
    public List<AuditEvent> getAuditEventsWithLaterTimeStamp(String className, int objectId, Date time) {
        String[] query =  new String[] {
                "select ip_address, user_name, time, class_name, operation, url, object_id, user_action_id",
                "from Audit_Events where class_name = ? and object_id = ? and time > ?"
        };
        return jdbcTemplate.query(join(query, ' '), new Object[] {className, objectId, time}, new AuditEventRowMappper(false));
    }

    @SuppressWarnings("unchecked")
    public List<AuditEvent> getAuditEventsWithValuesByUserActionId(String userActionId) {
        String[] query =  new String[] {
                "select id, ip_address, user_name, time, class_name, operation, url, object_id, user_action_id",
                "from Audit_Events where user_action_id = ?"
        };
        List<AuditEvent> events =  jdbcTemplate.query(join(query, ' '), new Object[] {userActionId}, new AuditEventRowMappper(true));
        Collections.sort(events);
        return events;
    }

    @SuppressWarnings("unchecked")
    private String determineDatabaseType() {
        String databaseType = (String) jdbcTemplate.execute(new ConnectionCallback() {
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getMetaData().getDatabaseProductName();
            }
        });
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public class AuditEventRowMappper implements RowMapper {
        boolean includeValues;
        public AuditEventRowMappper(boolean includeValues) {
            this.includeValues = includeValues;
        }

        public Object mapRow(ResultSet rs, int i) throws SQLException {
            AuditEventRecordResultSetExtractor extractor = new AuditEventRecordResultSetExtractor(includeValues);
            return extractor.extractData(rs);
        }
    }

    public class AuditEventRecordResultSetExtractor implements ResultSetExtractor {
        boolean includeValues;
        public AuditEventRecordResultSetExtractor(boolean includeValues) {
            this.includeValues = includeValues;
        }

        public Object extractData(ResultSet rs) throws SQLException {
            DataReference reference = new DataReference(rs.getString("class_name"), rs.getInt("object_id"));
            AuditEvent event = new AuditEvent();
            event.setReference(reference);
            event.setOperation(Operation.valueOf(rs.getString("operation")));
            event.getInfo().setUsername(rs.getString("user_name"));
            event.getInfo().setIp(rs.getString("ip_address"));
            event.getInfo().setTime(rs.getTimestamp("time"));
            event.getInfo().setUrl(rs.getString("url"));
            event.setUserActionId(rs.getString("user_action_id"));
            if (includeValues) {
                event.addValues(getAuditEventValuesForEvent(rs.getInt("id")));
            }
            return event;
        }
    }

    @SuppressWarnings("unchecked")
    public List<DataAuditEventValue> getAuditEventValuesForEvent(int eventId) {
        String[] query =  new String[] {
                "select attribute_name, previous_value, new_value",
                "from Audit_Event_Values where audit_event_id = ? order by id"
        };

        List<DataAuditEventValue> values =  jdbcTemplate.query(join(query, ' '),
                new Object[] {eventId}, new AuditEventValueRowMappper());
        return values;
    }

    public class AuditEventValueRowMappper implements RowMapper {
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            AuditEventValueRecordResultSetExtractor extractor = new AuditEventValueRecordResultSetExtractor();
            return extractor.extractData(rs);
        }
    }

    public class AuditEventValueRecordResultSetExtractor implements ResultSetExtractor {
        public Object extractData(ResultSet rs) throws SQLException {
            return new DataAuditEventValue(
                    rs.getString("attribute_name"),
                    rs.getString("previous_value"),
                    rs.getString("new_value"));
        }
    }
}
