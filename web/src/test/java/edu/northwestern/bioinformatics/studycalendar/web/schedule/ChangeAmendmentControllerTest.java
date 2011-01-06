package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPopulation;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static java.util.Calendar.AUGUST;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class ChangeAmendmentControllerTest  extends ControllerTestCase {
    private ChangeAmendmentController controller;
    private ChangeAmendmentCommand command;
    private ApplicationSecurityManager applicationSecurityManager;
    private UserActionDao userActionDao;
    private PscUser pscUser =   AuthorizationObjectFactory.createPscUser("user", 12L);
    private String applicationPath = "psc/application";
    private Set pops = new TreeSet<Population>();
    private DeltaService deltaService;
    private StudySubjectAssignment assignment;
    private Amendment a0, a1, a2, a3;

    protected void setUp() throws Exception {
        super.setUp();
        applicationSecurityManager = registerMockFor(ApplicationSecurityManager.class);
        deltaService = registerMockFor(DeltaService.class);
        userActionDao = registerDaoMockFor(UserActionDao.class);

        Study study = new Study();
        study.setAssignedIdentifier("Study A");
        StudySite ss = createStudySite(study, createSite("NU"));
        a3 = createAmendments("A0", "A1", "A2", "A3");
        a2 = a3.getPreviousAmendment();
        a1 = a2.getPreviousAmendment();
        a0 = a1.getPreviousAmendment();
        study.setAmendment(a3);
        ss.approveAmendment(a0, DateTools.createDate(2003, AUGUST, 1));
        ss.approveAmendment(a1, DateTools.createDate(2003, AUGUST, 2));
        ss.approveAmendment(a2, DateTools.createDate(2003, AUGUST, 3));

        Subject subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        assignment = setId(12, createAssignment(ss, subject));
        assignment.setCurrentAmendment(a0);
        Population pop = setId(11, createPopulation("T", "Test"));
        pops.add(pop);
        assignment.setPopulations(pops);

        command = new ChangeAmendmentCommand(assignment, deltaService);
        command.getAmendments().put(a0, false);
        command.getAmendments().put(a1, true);
        command.getAmendments().put(a2, true);
        controller = new ChangeAmendmentController();
        controller.setControllerTools(controllerTools);
        controller.setApplicationPath(applicationPath);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setDeltaService(deltaService);
        controller.setUserActionDao(userActionDao);
    }

    public void testOnSubmitSetsUserActionToAuditEvent() throws Exception {
        UserAction userAction = new UserAction();
        String context = applicationPath.concat("/api/v1/subjects/1111/schedules");
        userAction.setContext(context);
        userAction.setActionType("amendment");
        userAction.setUser(pscUser.getCsmUser());
        String des = "Amendment [01/02/2001 (A1)][01/03/2001 (A2)] applied to Perry Duglas for Study A";
        userAction.setDescription(des);
        expect(applicationSecurityManager.getUser()).andReturn(pscUser);
        deltaService.amend(assignment, a1);
        deltaService.amend(assignment, a2);
        userActionDao.save(userAction);
        replayMocks();

        controller.onSubmit(request, response, command, null);
        verifyMocks();

        UserAction actualUa = AuditEvent.getUserAction();
        assertNotNull("User Action is not set", actualUa);
        assertEquals("User action's context is different", context, actualUa.getContext());
        assertEquals("User action's actionType is different", "amendment", actualUa.getActionType());
        assertEquals("User action's des is different", des, actualUa.getDescription());
        assertEquals("User action's user is different", pscUser.getCsmUser(), actualUa.getUser());
    }
}

