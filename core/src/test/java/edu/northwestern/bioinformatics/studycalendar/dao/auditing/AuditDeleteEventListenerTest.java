package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import java.util.Date;

import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class AuditDeleteEventListenerTest extends StudyCalendarTestCase {
    private AuditDeleteEventListener listener;
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
        listener = new AuditDeleteEventListener();
        listener.setAuditEventHelper(auditEventHelper);
        info = new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("username", "10.10.10.155", new Date(), "/psc/pages/newStudy");
        DataAuditInfo.setLocal(info);
        study =  new Study();
    }

    public void testDeleteEventWhenStudyDeleted() throws Exception {
        PostDeleteEvent postDeleteEvent = new PostDeleteEvent(study, 14,
            new Object[] { null, null, null } , studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.DELETE, DataAuditInfo.copy(info));
        expect(auditEventHelper.createAuditEvent(study, Operation.DELETE)).andReturn(event);
        auditEventHelper.saveAuditEvent(event);
        fireEvent(postDeleteEvent);
    }

    public void testDeleteEventWhenEventIsNull() throws Exception {
        PostDeleteEvent postDeleteEvent = new PostDeleteEvent(study, 14,
            new Object[] { "Foo", "114", "G" }, studyPersister, null);
        expect(auditEventHelper.createAuditEvent(study, Operation.DELETE)).andReturn(null);
        fireEvent(postDeleteEvent);
    }

    private void fireEvent(PostDeleteEvent event) {
        replayMocks();
        listener.onPostDelete(event);
        verifyMocks();
    }
}
