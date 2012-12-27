/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.cabig.ctms.audit.exception.AuditSystemException;
import org.hibernate.type.AnyType;
import org.hibernate.type.SetType;
import org.hibernate.type.StringType;
import java.util.*;

/**
 * @author Jalpa Patel
 */
public class AuditEventFactoryTest extends StudyCalendarTestCase {
    private final String USERNAME = "testUser";
    private final String IP_ADDRESS = "10.10.10.155";
    private final String URL = "/psc/pages/newStudy";
    private DataAuditInfo info;
    private AuditEventFactory auditEventFactory;
    private Study study;
    private final String NAME = "name";
    private final String ID = "id";
    private AuditEvent ae0;
    public void setUp() throws Exception {
        super.setUp();
        auditEventFactory = new AuditEventFactory();
        info = new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo(USERNAME, IP_ADDRESS, new Date(), URL);
        DataAuditInfo.setLocal(info);
        study = new Study();
        study.setId(12);
        ae0 = new AuditEvent(study, Operation.UPDATE, info);
    }

    public void testCreateAuditEvent() throws Exception {
        replayMocks();
        DataAuditEvent event = auditEventFactory.createAuditEvent(study, Operation.CREATE);
        verifyMocks();

        assertNotNull("Event is not created", event);
        assertEquals("Wrong userName", USERNAME, event.getInfo().getUsername());
        assertEquals("Wrong IP address", IP_ADDRESS, event.getInfo().getIp());
        assertEquals("Wrong URL", URL, event.getInfo().getUrl());
        assertEquals("Wrong Class Name", "edu.northwestern.bioinformatics.studycalendar.domain.Study", event.getReference().getClassName());
    }

    public void testCreateAuditEventWhenEntityIsNotMutableDomainObject() throws Exception {
        Object obj = new Object();
        replayMocks();
        DataAuditEvent actualEvent = auditEventFactory.createAuditEvent(obj, Operation.CREATE);
        verifyMocks();
        assertNull("Event is created", actualEvent);
    }

    public void testCreateAuditEventWhenEntityDoesNotHaveId() throws Exception {
        replayMocks();
        DataAuditEvent actualEvent = auditEventFactory.createAuditEvent(new Study(), Operation.CREATE);
        verifyMocks();
        assertNull("Event is created", actualEvent);
    }

    public void testCreateAuditEventWhenNoLocalAuditInfo() throws Exception {
        DataAuditInfo.setLocal(null);
        replayMocks();
        try {
            auditEventFactory.createAuditEvent(study, Operation.CREATE);
            fail("Exception not thrown");
        } catch (AuditSystemException ase) {
            assertEquals("Can not audit; no local audit info available", ase.getMessage());
        }
        verifyMocks();
    }

    public void testCreateAuditEventWhenUserAction() throws Exception {
        UserAction ua = new UserAction();
        ua.setGridId("userActionId");
        AuditEvent.setUserAction(ua);
        replayMocks();
        AuditEvent event = auditEventFactory.createAuditEvent(study, Operation.CREATE);
        verifyMocks();

        assertNotNull("Event is not created", event);
        assertEquals("Wrong userAction id", "userActionId", event.getUserActionId());
    }

    public void testCreateAuditEventWhenNoUserAction() throws Exception {
        AuditEvent.setUserAction(null);
        replayMocks();
        AuditEvent event = auditEventFactory.createAuditEvent(study, Operation.CREATE);
        verifyMocks();

        assertNotNull("Event is not created", event);
        assertNull("UserAction id is attached to audit event", event.getUserActionId());
    }

    public void testAppendEventValuesForName() throws Exception {
        DataAuditEventValue expectedValue = new DataAuditEventValue(NAME, null, "testStudy");
        auditEventFactory.appendEventValues(ae0, new StringType(), NAME, null, "testStudy");
        assertNotNull(ae0.getValues());
        DataAuditEventValue actualValue = ae0.getValues().get(0);
        assertEquals("Property name is different", expectedValue.getAttributeName(), actualValue.getAttributeName());
        assertEquals("Previous value is different", expectedValue.getPreviousValue(), actualValue.getPreviousValue());
        assertEquals("Current value is different", expectedValue.getCurrentValue(), actualValue.getCurrentValue());
    }

    public void testAppendEventValuesForObjectId() throws Exception {
        Study study = new Study();
        study.setId(12);
        DataAuditEventValue expectedValue = new DataAuditEventValue(ID, null, study.getId().toString());
        auditEventFactory.appendEventValues(ae0, new StringType(), ID, null, study);
        assertNotNull(ae0.getValues());
        DataAuditEventValue actualValue = ae0.getValues().get(0);
        assertEquals("Property name is different", expectedValue.getAttributeName(), actualValue.getAttributeName());
        assertEquals("Previous value is different", expectedValue.getPreviousValue(), actualValue.getPreviousValue());
        assertEquals("Current value is different", expectedValue.getCurrentValue(), actualValue.getCurrentValue());
    }

    public void testAppendEventValuesForString() throws Exception {
        List<String> labels = new ArrayList<String>();
        labels.add("label1");
        labels.add("label2");
        labels.add("label3");
        DataAuditEventValue expectedValue = new DataAuditEventValue("label", null, "[label1, label2, label3]");
        auditEventFactory.appendEventValues(ae0, new StringType(), "label", null, labels);
        assertNotNull(ae0.getValues());
        DataAuditEventValue actualValue = ae0.getValues().get(0);
        assertEquals("Property name is different", expectedValue.getAttributeName(), actualValue.getAttributeName());
        assertEquals("Previous value is different", expectedValue.getPreviousValue(), actualValue.getPreviousValue());
        assertEquals("Current value is different", expectedValue.getCurrentValue(), actualValue.getCurrentValue());
    }

    public void testAppendEventValuesForPreviousAndCurrentStateNull() throws Exception {
        auditEventFactory.appendEventValues(ae0, new StringType(), NAME, null, null);
        assertEquals("Event value should not be added for null for both previous and current state", 0, ae0.getValues().size());
    }

    public void testAppendEventValueIfPreviousAndCurrentStateAreEquals() throws Exception {
        auditEventFactory.appendEventValues(ae0, new StringType(), NAME, "testName", "testName");
        assertEquals("Event value should not be added when previous and current state are equal", 0, ae0.getValues().size());
    }
}

