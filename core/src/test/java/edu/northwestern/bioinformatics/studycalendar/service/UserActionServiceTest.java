package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.dao.auditing.AuditEventDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createInDevelopmentTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
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
    private final String URL = "/psc/pages/updateStudy";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    public void setUp() throws Exception {
        super.setUp();

        userActionDao = registerDaoMockFor(UserActionDao.class);
        auditEventDao = registerDaoMockForNonStudyCalendarDao(AuditEventDao.class);
        applicationSecurityManager = registerMockFor(ApplicationSecurityManager.class);

        service =  new UserActionService();
        service.setAuditEventDao(auditEventDao);
        service.setUserActionDao(userActionDao);
        service.setApplicationSecurityManager(applicationSecurityManager);

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

    private void expectCurrentUser() {
        expect(applicationSecurityManager.getUserName()).andReturn(USER_NAME);
    }
}
