package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import java.util.Date;

import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class AuditUpdateEventListenerTest extends StudyCalendarTestCase {
    private AuditUpdateEventListener listener;
    private EntityPersister studyPersister;
    private DataAuditInfo info;
    private AuditEventHelper auditEventHelper;
    private Study study;
    private Type[] types = {new StringType(), new StringType(), new StringType()};
    public void setUp() throws Exception {
        super.setUp();

        auditEventHelper = registerMockFor(AuditEventHelper.class);
        studyPersister = registerMockFor(EntityPersister.class);
        expect(studyPersister.getPropertyNames()).andStubReturn(
            new String[] { "longTitle", "assignedIdentifier", "gridId" });
        expect(studyPersister.getPropertyTypes()).andStubReturn(types);
        listener = new AuditUpdateEventListener();
        listener.setAuditEventHelper(auditEventHelper);
        info = new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("username", "10.10.10.155", new Date(), "/psc/pages/newStudy");
        DataAuditInfo.setLocal(info);
        study = new Study();
    }

    public void testUpdateEventWhenStudyLongTitleChanged() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Bar", "114", "G" }, studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(info));
        expect(auditEventHelper.createAuditEvent(study, Operation.UPDATE)).andReturn(event);
        auditEventHelper.saveAuditEvent(event);

        fireEvent(postUpdateEvent);
    }

    public void testUpdateEventWhenStudyIdentifierChanged() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Foo", "141", "G" }, studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(info));
        expect(auditEventHelper.createAuditEvent(study, Operation.UPDATE)).andReturn(event);
        auditEventHelper.saveAuditEvent(event);

        fireEvent(postUpdateEvent);
    }

    public void testUpdateEventWhenStudyGridIdChanged() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Foo", "114", "Grid1" }, studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(info));
        expect(auditEventHelper.createAuditEvent(study, Operation.UPDATE)).andReturn(event);
        auditEventHelper.saveAuditEvent(event);

        fireEvent(postUpdateEvent);
    }

    public void testSaveEventWhenEventIsNull() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Foo", "114", "G" }, studyPersister, null);
        expect(auditEventHelper.createAuditEvent(study, Operation.UPDATE)).andReturn(null);

        fireEvent(postUpdateEvent);
    }

    public void testDoesNothingWhenOldStateUnknown() throws Exception {
         PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 9,
            new Object[] { "0309", "G", "F" },
            null, studyPersister, null);
         fireEvent(postUpdateEvent);
    }

    private void fireEvent(PostUpdateEvent event) {
        replayMocks();
        listener.onPostUpdate(event);
        verifyMocks();
    }
}