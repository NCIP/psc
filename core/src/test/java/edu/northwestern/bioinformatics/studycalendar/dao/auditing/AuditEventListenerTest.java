/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.service.auditing.AuditEventFactory;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import java.util.Date;

import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class AuditEventListenerTest extends StudyCalendarTestCase {
    private AuditEventListener listener;
    private EntityPersister studyPersister;
    private DataAuditInfo info;
    private AuditEventFactory auditEventFactory;
    private AuditEventDao auditEventDao;
    private Study study;
    private Type[] types = {new StringType(), new StringType(), new StringType()};

    public void setUp() throws Exception {
        super.setUp();

        auditEventFactory= registerMockFor(AuditEventFactory.class);
        auditEventDao= registerMockFor(AuditEventDao.class);
        studyPersister = registerMockFor(EntityPersister.class);
        expect(studyPersister.getPropertyNames()).andStubReturn(
            new String[] { "longTitle", "assignedIdentifier", "gridId" });
        expect(studyPersister.getPropertyTypes()).andStubReturn(types);
        listener = new AuditEventListener();
        listener.setAuditEventFactory(auditEventFactory);
        listener.setAuditEventDao(auditEventDao);
        info = new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("username", "10.10.10.155", new Date(), "/psc/pages/newStudy");
        DataAuditInfo.setLocal(info);
        study = new Study();
    }

    public void testUpdateEventWhenStudyLongTitleChanged() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Bar", "114", "G" },
            new int[0], studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(info));
        expect(auditEventFactory.createAuditEvent(study, Operation.UPDATE)).andReturn(event);
        auditEventFactory.appendEventValues(event, types[0], "longTitle", "Bar", "Foo");
        auditEventFactory.appendEventValues(event, types[1], "assignedIdentifier", "114", "114");
        auditEventFactory.appendEventValues(event, types[2], "gridId", "G", "G");
        auditEventDao.saveEvent(event);

        firePostUpdateEvent(postUpdateEvent);
    }

    public void testUpdateEventWhenStudyIdentifierChanged() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Foo", "141", "G" },
            new int[0], studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(info));
        expect(auditEventFactory.createAuditEvent(study, Operation.UPDATE)).andReturn(event);
        auditEventFactory.appendEventValues(event, types[0], "longTitle", "Foo", "Foo");
        auditEventFactory.appendEventValues(event, types[1], "assignedIdentifier", "141", "114");
        auditEventFactory.appendEventValues(event, types[2], "gridId", "G", "G");
        auditEventDao.saveEvent(event);

        firePostUpdateEvent(postUpdateEvent);
    }

    public void testUpdateEventWhenStudyGridIdChanged() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Foo", "114", "Grid1" },
            new int[0], studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(info));
        expect(auditEventFactory.createAuditEvent(study, Operation.UPDATE)).andReturn(event);
        auditEventFactory.appendEventValues(event, types[0], "longTitle", "Foo", "Foo");
        auditEventFactory.appendEventValues(event, types[1], "assignedIdentifier", "114", "114");
        auditEventFactory.appendEventValues(event, types[2], "gridId", "Grid1", "G");
        auditEventDao.saveEvent(event);

        firePostUpdateEvent(postUpdateEvent);
    }

    public void testUpdateEventWhenEventIsNull() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Foo", "114", "G" },
            new int[0], studyPersister, null);
        expect(auditEventFactory.createAuditEvent(study, Operation.UPDATE)).andReturn(null);

        firePostUpdateEvent(postUpdateEvent);
    }

    public void testUpdateEventDoesNothingWhenOldStateUnknown() throws Exception {
        PostUpdateEvent postUpdateEvent = new PostUpdateEvent(study, 9,
            new Object[] { "0309", "G", "F" },
            null,
            new int[0], studyPersister, null);
        firePostUpdateEvent(postUpdateEvent);
    }

    public void testCreateEventWhenNewEvent() throws Exception {
        PostInsertEvent postInsertEvent = new PostInsertEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.CREATE, DataAuditInfo.copy(info));
        expect(auditEventFactory.createAuditEvent(study, Operation.CREATE)).andReturn(event);
        auditEventFactory.appendEventValues(event, types[0], "longTitle", null, "Foo");
        auditEventFactory.appendEventValues(event, types[1], "assignedIdentifier", null, "114");
        auditEventFactory.appendEventValues(event, types[2], "gridId", null, "G");
        auditEventDao.saveEvent(event);

        firePostInsertEvent(postInsertEvent);
    }

    public void testCreateEventWhenEventIsNull() throws Exception {
        PostInsertEvent postInsertEvent = new PostInsertEvent(study, 14,
            new Object[] { "Foo", "114", "G" },
            studyPersister, null);
        expect(auditEventFactory.createAuditEvent(study, Operation.CREATE)).andReturn(null);

        firePostInsertEvent(postInsertEvent);
    }

    public void testDeleteEventWhenStudyDeleted() throws Exception {
        PostDeleteEvent postDeleteEvent = new PostDeleteEvent(study, 14,
            new Object[] { "Foo", "114", "G"  } , studyPersister, null);
        AuditEvent event = new AuditEvent(study, Operation.DELETE, DataAuditInfo.copy(info));
        expect(auditEventFactory.createAuditEvent(study, Operation.DELETE)).andReturn(event);
        auditEventFactory.appendEventValues(event, types[0], "longTitle", "Foo", null);
        auditEventFactory.appendEventValues(event, types[1], "assignedIdentifier", "114", null);
        auditEventFactory.appendEventValues(event, types[2], "gridId", "G", null);
        auditEventDao.saveEvent(event);
        firePostDeleteEvent(postDeleteEvent);
    }

    public void testDeleteEventWhenEventIsNull() throws Exception {
        PostDeleteEvent postDeleteEvent = new PostDeleteEvent(study, 14,
            new Object[] { "Foo", "114", "G" }, studyPersister, null);
        expect(auditEventFactory.createAuditEvent(study, Operation.DELETE)).andReturn(null);
        firePostDeleteEvent(postDeleteEvent);
    }

    private void firePostDeleteEvent(PostDeleteEvent event) {
        replayMocks();
        listener.onPostDelete(event);
        verifyMocks();
    }

    private void firePostInsertEvent(PostInsertEvent event) {
        replayMocks();
        listener.onPostInsert(event);
        verifyMocks();
    }

    private void firePostUpdateEvent(PostUpdateEvent event) {
        replayMocks();
        listener.onPostUpdate(event);
        verifyMocks();
    }
}