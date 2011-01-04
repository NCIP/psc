package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import java.util.List;
import java.util.Map;

/**
 * @author Jalpa Patel
 */                                                                                                                         
public class AuditEventDaoTest extends DaoTestCase {
    private AuditEventDao dao = (AuditEventDao) getApplicationContext().getBean("auditEventDao");

    public void setUp() throws Exception {
        super.setUp();
    }

    @SuppressWarnings({ "unchecked" })
    public void testSaveNewEvent() throws Exception {
        {
            AuditEvent event = new AuditEvent(edu.northwestern.bioinformatics.studycalendar.domain.StudySegment.class, Operation.CREATE, (DataAuditInfo)gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo.getLocal());
            event.getReference().setId(13);
            dao.saveEvent(event);
        }

        interruptSession();

        List<AuditEvent> events = getJdbcTemplate().queryForList("select * from audit_events where object_id = 13");
        assertNotNull(events);
        assertEquals("No of events are wrong", 1, events.size());
    }

    @SuppressWarnings({ "unchecked" })
    public void testSaveEventWithValues() throws Exception {
         {
             AuditEvent event = new AuditEvent(edu.northwestern.bioinformatics.studycalendar.domain.StudySegment.class, Operation.CREATE, (DataAuditInfo)gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo.getLocal());
             event.getReference().setId(13);
             event.addValue(new DataAuditEventValue("name", null, "testStudy"));
             event.addValue(new DataAuditEventValue("assignedIdentifier", null, "assignId"));
             event.addValue(new DataAuditEventValue("gridId", null, "gridIdentifier"));
             dao.saveEvent(event);
        }

        interruptSession();

        List<AuditEvent> events = getJdbcTemplate().queryForList("select * from audit_events where object_id = 13");
        assertNotNull(events);
        assertEquals("No of events are wrong", 1, events.size());

        int eventId = getJdbcTemplate().queryForInt("select id from audit_events where object_id = 13");
        assertNotNull("Event is not created", eventId);

        List<Map> eventValues = getJdbcTemplate().queryForList("select * from audit_event_values where audit_event_id = ?", new Object[] {eventId});
        assertEquals("No of values saved are different", 3, eventValues.size());
    }
}
