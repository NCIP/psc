/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class ChangePopulationsControllerTest extends ControllerTestCase {
    private ChangePopulationsController controller;
    private ChangePopulationsCommand command;
    private SubjectService subjectService;
    private ApplicationSecurityManager applicationSecurityManager;
    private UserActionDao userActionDao;
    private PscUser pscUser =   AuthorizationObjectFactory.createPscUser("user",12L);
    private String applicationPath = "psc/application";
    private StudySubjectAssignment assignment;
    private Set pops = new TreeSet<Population>();

    protected void setUp() throws Exception {
        super.setUp();
        applicationSecurityManager = registerMockFor(ApplicationSecurityManager.class);
        subjectService = registerMockFor(SubjectService.class);
        userActionDao = registerDaoMockFor(UserActionDao.class);
        Subject subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        subject.setGridId("1111");
        assignment = setId(12, createAssignment(createBasicTemplate("Joe's Study")
                , createSite("NU"), subject));
        Population pop = setId(11, createPopulation("T", "Test"));
        pops.add(pop);
        assignment.setPopulations(pops);

        command = new ChangePopulationsCommand(assignment, subjectService);
        controller = new ChangePopulationsController();
        controller.setControllerTools(controllerTools);
        controller.setApplicationPath(applicationPath);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setSubjectService(subjectService);
        controller.setUserActionDao(userActionDao);
    }

    public void testOnSubmitSetsUserActionToAuditEvent() throws Exception {
        UserAction userAction = new UserAction();
        String context = applicationPath.concat("/api/v1/subjects/1111/schedules");
        userAction.setContext(context);
        userAction.setActionType("population");
        userAction.setUser(pscUser.getCsmUser());
        String des = "Population changed to [T: Test] for Perry Duglas for Joe's Study";
        userAction.setDescription(des);
        expect(applicationSecurityManager.getUser()).andReturn(pscUser);
        userActionDao.save(userAction);
        subjectService.updatePopulations(assignment, pops);
        replayMocks();
        controller.onSubmit(request, response, command, null);
        verifyMocks();

        UserAction actualUa = AuditEvent.getUserAction();
        assertNotNull("User Action is not set", actualUa);
        assertEquals("User action's context is different", context, actualUa.getContext());
        assertEquals("User action's actionType is different", "population", actualUa.getActionType());
        assertEquals("User action's des is different", des, actualUa.getDescription());
        assertEquals("User action's user is different", pscUser.getCsmUser(), actualUa.getUser());
    }

    public void testOnSubmitSetsUserActionToAuditEventForPopulationChangesToNone() throws Exception {
        command.setPopulations(null);
        UserAction userAction = new UserAction();
        String context = applicationPath.concat("/api/v1/subjects/1111/schedules");
        userAction.setContext(context);
        userAction.setActionType("population");
        userAction.setUser(pscUser.getCsmUser());
        String des = "Population changed to none for Perry Duglas for Joe's Study";
        userAction.setDescription(des);
        expect(applicationSecurityManager.getUser()).andReturn(pscUser);
        userActionDao.save(userAction);
        subjectService.updatePopulations(assignment, null);
        replayMocks();
        controller.onSubmit(request, response, command, null);
        verifyMocks();

        UserAction actualUa = AuditEvent.getUserAction();
        assertNotNull("User Action is not set", actualUa);
        assertEquals("User action's context is different", context, actualUa.getContext());
        assertEquals("User action's actionType is different", "population", actualUa.getActionType());
        assertEquals("User action's des is different", des, actualUa.getDescription());
        assertEquals("User action's user is different", pscUser.getCsmUser(), actualUa.getUser());
    }
}
