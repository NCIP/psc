package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public void testGetAuditEventsByUserActionId() throws Exception {
        interruptSession();
        List<AuditEvent> auditEvents = dao.getAuditEventsByUserActionId("grid1");
        assertNotNull("Audit Events not found", auditEvents);
        assertEquals("No of audit events are wrong", 2, auditEvents.size());
        AuditEvent first = auditEvents.get(0);

        assertEquals("Wrong class_name", "edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity",
                first.getReference().getClassName());
        assertEquals("Wrong Operation", "UPDATE", first.getOperation().toString());

        AuditEvent second = auditEvents.get(1);
        assertEquals("Wrong class_name", "edu.northwestern.bioinformatics.studycalendar.domain.Activity",
                second.getReference().getClassName());
        assertEquals("Wrong Operation", "CREATE", second.getOperation().toString());
    }

    public void testGetAuditEventsBySearchCriteria() throws Exception {
        interruptSession();
        String className = "edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity";
        int objectId = -13;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Date time = sdf.parse("2010-08-17 23:26:58.361");
        List<AuditEvent> auditEvents = dao.getAuditEventsWithLaterTimeStamp(className, objectId, time);
        assertNotNull("Audit Events not found", auditEvents);
        assertEquals("No of audit events are wrong", 1, auditEvents.size());
        AuditEvent first = auditEvents.get(0);

        assertEquals("Wrong Operation", "CREATE", first.getOperation().toString());
    }
}