package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.DataReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
}
