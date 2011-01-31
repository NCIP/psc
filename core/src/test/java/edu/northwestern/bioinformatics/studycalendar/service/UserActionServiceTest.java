package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.auditing.AuditEventDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.security.authorization.domainobjects.User;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class UserActionServiceTest extends StudyCalendarTestCase {
    private UserActionService service;
    private UserActionDao userActionDao;
    private AuditEventDao auditEventDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private UserAction ua1, ua2, ua3, ua4;
    private User csmUser1, csmUser2;
    private Study s1, s2, s3;
    private AuditEvent ae1, ae2, ae3, ae4;
    private AuditEvent ae1Ua, ae2Ua,ae3Ua;
    private final String USER_NAME = "cathy";
    private final String IP = "10.10.10.155";
    private final String URL = "/psc/pages/update";
    private final SimpleDateFormat sdf = DateFormat.getUTCFormat();
    private DaoFinder daoFinder;
    private DeletableDomainObjectDao domainObjectDao;

    public void setUp() throws Exception {
        super.setUp();

        daoFinder = registerMockFor(DaoFinder.class);
        domainObjectDao = registerMockFor(DeletableDomainObjectDao.class);
        userActionDao = registerDaoMockFor(UserActionDao.class);
        auditEventDao = registerMockFor(AuditEventDao.class);
        applicationSecurityManager = registerMockFor(ApplicationSecurityManager.class);
        service =  new UserActionService();
        service.setAuditEventDao(auditEventDao);
        service.setUserActionDao(userActionDao);
        service.setApplicationSecurityManager(applicationSecurityManager);
        service.setDaoFinder(daoFinder);

        csmUser1 = AuthorizationObjectFactory.createCsmUser(11, USER_NAME);
        csmUser2 = AuthorizationObjectFactory.createCsmUser(12, "peri");
        SecurityContextHolderTestHelper.setSecurityContext(createPscUser(csmUser1));

        s1 = setId(11, createInDevelopmentTemplate("S1"));
        s2 = setId(12, createInDevelopmentTemplate("S2"));
        s3 = setId(13, createInDevelopmentTemplate("S3"));
    }

    public void testGetUndoableActionsDoNotIncludeUAOfOtherUser() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context", "actionType1", true, csmUser2));
        ua1.setTime(sdf.parse("2010-08-17 10:43:58.361"));

        expect(userActionDao.getUserActionsByContext("context")).andReturn(Arrays.asList(ua1));
        expectCurrentUser();

        replayMocks();
        List<UserAction> undoableActions = service.getUndoableActions("context");
        verifyMocks();

        assertEquals("Undoable actions contain user action", 0, undoableActions.size());
    }

    public void testUndoableActionsDoNotIncludeAlreadyUndoneUA() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context", "actionType1", true, csmUser1));

        expect(userActionDao.getUserActionsByContext("context")).andReturn(Arrays.asList(ua1));
        expectCurrentUser();

        replayMocks();
        List<UserAction> undoableActions = service.getUndoableActions("context");
        verifyMocks();

        assertEquals("Undoable actions contain user action", 0, undoableActions.size());
    }

    public void testDoesNotIncludeUAIfAuditEventForSameObjectWithLaterTimeStempAndNullUA() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:43:58.361"));

        ae1 = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:45:58.361"), URL));
        ae1Ua = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:43:58.361"), URL), ua1);

        expect(userActionDao.getUserActionsByContext("context")).andReturn(Arrays.asList(ua1));
        expectCurrentUser();
        expect(auditEventDao.getAuditEventsByUserActionId("ua1")).andReturn(Arrays.asList(ae1Ua));
        expect(auditEventDao.getAuditEventsWithLaterTimeStamp(ae1Ua.getReference().getClassName(),
                ae1Ua.getReference().getId(), ae1Ua.getInfo().getTime())).andReturn(Arrays.asList(ae1));

        replayMocks();
        List<UserAction> undoableActions = service.getUndoableActions("context");
        verifyMocks();

        assertEquals("Undoable actions contain user action", 0, undoableActions.size());
    }

    public void testDoesNotIncludeUAIfAeForSameObjectWithLaterTimeStempAndOtherUAUser() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:43:58.361"));

        ua2 = setGridId("ua2", new UserAction("description3", "context", "actionType3", false, csmUser2));
        ua2.setTime(sdf.parse("2010-08-17 10:42:58.361"));

        ae1 = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:45:58.361"), URL), ua2);
        ae1Ua = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:43:58.361"), URL), ua1);
        expect(userActionDao.getUserActionsByContext("context")).andReturn(Arrays.asList(ua1));
        expectCurrentUser();

        expect(auditEventDao.getAuditEventsByUserActionId("ua1")).andReturn(Arrays.asList(ae1Ua));
        expect(auditEventDao.getAuditEventsWithLaterTimeStamp(ae1Ua.getReference().getClassName(),
                ae1Ua.getReference().getId(), ae1Ua.getInfo().getTime())).andReturn(Arrays.asList(ae1));
        expect(userActionDao.getByGridId("ua2")).andReturn(ua2);

        replayMocks();
        List<UserAction> undoableActions = service.getUndoableActions("context");
        verifyMocks();

        assertEquals("Undoable actions contain user action", 0, undoableActions.size());
    }

    public void testDoesNotIncludeUAIfAeForSameObjectWithLaterTimeStempAndOtherUAContext() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:43:58.361"));

        ua2 = setGridId("ua2", new UserAction("description3", "context1", "actionType3", false, csmUser1));
        ua2.setTime(sdf.parse("2010-08-17 10:42:58.361"));

        ae1 = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:45:58.361"), URL), ua2);
        ae1Ua = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:43:58.361"), URL), ua1);
        expect(userActionDao.getUserActionsByContext("context")).andReturn(Arrays.asList(ua1));
        expectCurrentUser();

        expect(auditEventDao.getAuditEventsByUserActionId("ua1")).andReturn(Arrays.asList(ae1Ua));
        expect(auditEventDao.getAuditEventsWithLaterTimeStamp(ae1Ua.getReference().getClassName(),
                ae1Ua.getReference().getId(), ae1Ua.getInfo().getTime())).andReturn(Arrays.asList(ae1));
        expect(userActionDao.getByGridId("ua2")).andReturn(ua2);

        replayMocks();
        List<UserAction> undoableActions = service.getUndoableActions("context");
        verifyMocks();

        assertEquals("Undoable actions contain user action", 0, undoableActions.size());
    }

    public void testGetUndoableActionsList() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser2));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));

        ua2 = setGridId("ua2", new UserAction("description2", "context", "actionType2", false, csmUser1));
        ua2.setTime(sdf.parse("2010-08-17 10:27:58.361"));

        ua3 = setGridId("ua3", new UserAction("description3", "context", "actionType3", false, csmUser1));
        ua3.setTime(sdf.parse("2010-08-17 10:19:58.361"));

        ua4 = setGridId("ua4", new UserAction("description4", "context", "actionType4", false, csmUser1));
        ua4.setTime(sdf.parse("2010-08-17 10:33:58.361"));

        ae1 = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae2 = new AuditEvent(s2, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:45:58.361"), URL), ua2);
        ae3 = new AuditEvent(s3, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:46:58.361"), URL), ua2);
        ae4 = new AuditEvent(s3, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:47:58.361"), URL), ua3);

        ae1Ua = new AuditEvent(s1, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:43:58.361"), URL), ua2);
        ae2Ua = new AuditEvent(s2, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:43.361"), URL), ua3);
        ae3Ua = new AuditEvent(s3, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:43:58.361"), URL), ua4);

        expect(userActionDao.getUserActionsByContext("context")).andReturn(Arrays.asList(ua3, ua2, ua4));

        expectCurrentUser();
        expect(auditEventDao.getAuditEventsByUserActionId("ua4")).andReturn(Arrays.asList(ae3Ua));
        expect(auditEventDao.getAuditEventsWithLaterTimeStamp(s3.getClass().getName(), s3.getId(), ae3Ua.getInfo().getTime())).andReturn(Arrays.asList(ae3,ae4));
        expect(userActionDao.getByGridId("ua2")).andReturn(ua2);
        expect(userActionDao.getByGridId("ua3")).andReturn(ua3);

        expectCurrentUser();
        expect(auditEventDao.getAuditEventsByUserActionId("ua2")).andReturn(Arrays.asList(ae1Ua));
        expect(auditEventDao.getAuditEventsWithLaterTimeStamp(s1.getClass().getName(), s1.getId(), ae1Ua.getInfo().getTime())).andReturn(Arrays.asList(ae1));
        expect(userActionDao.getByGridId("ua1")).andReturn(ua1);

        expectCurrentUser();
        expect(auditEventDao.getAuditEventsByUserActionId("ua3")).andReturn(Arrays.asList(ae2Ua));
        expect(auditEventDao.getAuditEventsWithLaterTimeStamp(s2.getClass().getName(), s2.getId(), ae2Ua.getInfo().getTime())).andReturn(Arrays.asList(ae2));
        expect(userActionDao.getByGridId("ua2")).andReturn(ua2);

        replayMocks();
        List<UserAction> undoableActions = service.getUndoableActions("context");
        verifyMocks();

        assertEquals("No undoable actions", 2, undoableActions.size());
        assertEquals("Undoable actions are not in order", Arrays.asList(ua4, ua3), undoableActions);

    }

    @SuppressWarnings({ "unchecked" })
    public void testApplyUndoForUpdateAuditEvents() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));
        Site site = setId(11, createSite("UpdatedSite", "S1"));
        ae1 = new AuditEvent(site, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae1.addValue(new DataAuditEventValue("name", "Site", "UpdatedSite"));

        expect(auditEventDao.getAuditEventsWithValuesByUserActionId("ua1")).andReturn(Arrays.asList(ae1));
        expect(daoFinder.findDao(Site.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(11)).andReturn(site);
        assertFalse("UserAction is undone", ua1.isUndone());
        assertEquals("Site name is not undone", "UpdatedSite", site.getName());
        domainObjectDao.save(site);
        replayMocks();
        service.applyUndo(ua1);
        verifyMocks();

        assertTrue("UserAction is not undone", ua1.isUndone());
        assertEquals("Site name is undone", "Site", site.getName());
    }

    @SuppressWarnings({ "unchecked" })
    public void testApplyUndoForComplexUpdateAuditEvents() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));
        ScheduledActivityState state =  new ScheduledActivityState(ScheduledActivityMode.CANCELED,
                DateTools.createDate(2010, Calendar.OCTOBER, 18), "Just Canceled");
        ScheduledActivity event = setId(1, createScheduledActivityWithStudy("DC", 2010, Calendar.OCTOBER, 18, state));
        event.getCurrentState().setReason("Delay for 1 day");
        event.setNotes("note1");
        ae1 = new AuditEvent(event, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae1.addValue(new DataAuditEventValue("notes", "note", "note1"));
        ae1.addValue(new DataAuditEventValue("currentState.reason", "Initialized from template", "Delay for 1 day"));
        ae1.addValue(new DataAuditEventValue("currentState.date", "2010-10-17T00:00:00.0Z", "2010-10-18T00:00:00.0Z"));
        ae1.addValue(new DataAuditEventValue("currentState.mode", "1", "3"));

        expect(auditEventDao.getAuditEventsWithValuesByUserActionId("ua1")).andReturn(Arrays.asList(ae1));
        expect(daoFinder.findDao(ScheduledActivity.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(1)).andReturn(event);
        assertFalse("UserAction is undone", ua1.isUndone());
        assertEquals("Scheduled Activity is not undone ", ScheduledActivityMode.CANCELED, event.getCurrentState().getMode());
        domainObjectDao.save(event);

        replayMocks();
        service.applyUndo(ua1);
        verifyMocks();

        assertTrue("UserAction is not undone", ua1.isUndone());
        assertEquals("Scheduled Activity is undone", "note", event.getNotes());
        assertEquals("Scheduled Activity is undone", "Initialized from template", event.getCurrentState().getReason());
        assertEquals("Scheduled Activity is undone", "Sun Oct 17 00:00:00 CDT 2010", event.getCurrentState().getDate().toString());
        assertEquals("Scheduled Activity is undone", ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
    }

    @SuppressWarnings({ "unchecked" })
    public void testApplyUndoForUpdateAuditEventForAmendmentApply() throws Exception {
        AmendmentDao amendmentDao =  registerDaoMockFor(AmendmentDao.class);
        service.setAmendmentDao(amendmentDao);
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));
        Amendment a2 = setId(12, createAmendment("New A", DateTools.createDate(2010, Calendar.OCTOBER, 18), false));
        Amendment a1 = setId(11, createAmendment("A", DateTools.createDate(2010, Calendar.OCTOBER, 17), false));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setCurrentAmendment(a2);
        setId(1, assignment);
        ae1 = new AuditEvent(assignment, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae1.addValue(new DataAuditEventValue("currentAmendment", "11", "12"));
        expect(auditEventDao.getAuditEventsWithValuesByUserActionId("ua1")).andReturn(Arrays.asList(ae1));
        expect(daoFinder.findDao(StudySubjectAssignment.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(1)).andReturn(assignment);
        assertFalse("UserAction is undone", ua1.isUndone());
        expect(amendmentDao.getById(11)).andReturn(a1);
        assertEquals("StudySubjectAssignment is undone", a2, assignment.getCurrentAmendment());
        domainObjectDao.save(assignment);

        replayMocks();
        service.applyUndo(ua1);
        verifyMocks();

        assertTrue("UserAction is not undone", ua1.isUndone());
        assertEquals("StudySubjectAssignment is undone", a1, assignment.getCurrentAmendment());
    }

    public void testApplyDoNotSaveEntityIfOnlyVersionIsUpdated() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));
        Site site = setId(11, createSite("Site", "S1"));
        ae1 = new AuditEvent(site, Operation.UPDATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae1.addValue(new DataAuditEventValue("version", "0", "1"));

        expect(auditEventDao.getAuditEventsWithValuesByUserActionId("ua1")).andReturn(Arrays.asList(ae1));
        expect(daoFinder.findDao(Site.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(11)).andReturn(site);
        assertFalse("UserAction is undone", ua1.isUndone());

        replayMocks();
        service.applyUndo(ua1);
        verifyMocks();

        assertTrue("UserAction is not undone", ua1.isUndone());
     }

    @SuppressWarnings({ "unchecked" })
    public void testApplyUndoForCreateAuditEvents() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));
        Site site = setId(11, createSite("Site", "S1"));
        ae1 = new AuditEvent(site, Operation.CREATE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae1.addValue(new DataAuditEventValue("name", null, "Site"));
        ae1.addValue(new DataAuditEventValue("assignedIdentifier", null, "S1"));

        expect(auditEventDao.getAuditEventsWithValuesByUserActionId("ua1")).andReturn(Arrays.asList(ae1));
        expect(daoFinder.findDao(Site.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(11)).andReturn(site);
        assertFalse("UserAction is undone", ua1.isUndone());
        domainObjectDao.delete(site);

        replayMocks();
        service.applyUndo(ua1);
        verifyMocks();

        assertTrue("UserAction is not undone", ua1.isUndone());
    }

    @SuppressWarnings({ "unchecked" })
    public void testApplyUndoForDeleteAuditEvents() throws Exception {
        ua1 = setGridId("ua1", new UserAction("description1", "context1", "actionType1", false, csmUser1));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));
        Site site = setId(11, createSite("Site", "S1"));
        ae1 = new AuditEvent(site, Operation.DELETE, new DataAuditInfo(USER_NAME, IP, sdf.parse("2010-08-17 10:44:58.361"), URL), ua1);
        ae1.addValue(new DataAuditEventValue("name", "Site", null));
        ae1.addValue(new DataAuditEventValue("assignedIdentifier","S1", null));

        expect(auditEventDao.getAuditEventsWithValuesByUserActionId("ua1")).andReturn(Arrays.asList(ae1));
        expect(daoFinder.findDao(Site.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(11)).andReturn(null);
        assertFalse("UserAction is undone", ua1.isUndone());

        Site newSite = createSite("Site", "S1");
        domainObjectDao.save(newSite);

        replayMocks();
        service.applyUndo(ua1);
        verifyMocks();

        assertTrue("UserAction is not undone", ua1.isUndone());
    }

    private void expectCurrentUser() {
        expect(applicationSecurityManager.getUserName()).andReturn(USER_NAME);
    }
}
