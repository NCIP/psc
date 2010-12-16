package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import java.util.*;

import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class AuditCreateEventListenerTest extends StudyCalendarTestCase {
    private AuditCreateEventListener listener;
    private EntityPersister studyPersister;
    private DataAuditInfo info;
    private AuditEventHelper auditEventHelper;
    private Type[] types = {new StringType(), new StringType(), new StringType()};
    private Study study;

    public void setUp() throws Exception {
        super.setUp();

        auditEventHelper = registerMockFor(AuditEventHelper.class);
        studyPersister = registerMockFor(EntityPersister.class);
        expect(studyPersister.getPropertyNames()).andStubReturn(
            new String[] { "longTitle", "assignedIdentifier", "gridId" });
        expect(studyPersister.getPropertyTypes()).andStubReturn(types);
        listener = new AuditCreateEventListener();
        listener.setAuditEventHelper(auditEventHelper);
         info = new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("username", "10.10.10.155", new Date(), "/psc/pages/newStudy");
        DataAuditInfo.setLocal(info);
        study =  new Study();
    }

    public void testSaveEventWhenNewEvent() throws Exception {
        PostInsertEvent postInsertEvent = new PostInsertEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.CREATE, DataAuditInfo.copy(info));
        expect(auditEventHelper.createAuditEvent(study, Operation.CREATE)).andReturn(event);
        auditEventHelper.saveAuditEvent(event);

        fireEvent(postInsertEvent);
    }

    public void testSaveEventWhenEventIsNull() throws Exception {
        PostInsertEvent postInsertEvent = new PostInsertEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            studyPersister, null);
        expect(auditEventHelper.createAuditEvent(study, Operation.CREATE)).andReturn(null);

        fireEvent(postInsertEvent);
    }

    private void fireEvent(PostInsertEvent event) {
        replayMocks();
        listener.onPostInsert(event);
        verifyMocks();
    }
}
