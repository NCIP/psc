/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SubjectOffStudyControllerTest extends ControllerTestCase{

    private SubjectService subjectService;
    private StudySubjectAssignmentDao assignmentDao;
    private SubjectOffStudyController controller;
    private StudySubjectAssignment assignment;
    private SubjectOffStudyCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        command = registerMockFor(SubjectOffStudyCommand.class, SubjectOffStudyCommand.class.getMethod("takeSubjectOffStudy"));
        subjectService = registerMockFor(SubjectService.class);
        assignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        controller = new SubjectOffStudyController(){
           @Override protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setSubjectService(subjectService);
        controller.setStudySubjectAssignmentDao (assignmentDao);
        controller.setControllerTools(controllerTools);

        // Stop controller from calling validation
        controller.setValidateOnBinding(false);

        assignment = setId(10, new StudySubjectAssignment());
        assignment.setScheduledCalendar(setId(20, new ScheduledCalendar()));
    }

    public void testAuthorizedRoles() {
        Study study = setId(100, edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate());
        study.setAssignedIdentifier("100");
        Site site = Fixtures.createSite("site", "5");
        site.setId(5);
        StudySite studySite = Fixtures.createStudySite(study, site);
        assignment.setStudySite(studySite);
        Map<String, String[]> params = new HashMap<String, String[]>();
        String[] assignmentId = {assignment.getId().toString()};
        params.put("assignment", assignmentId);
        expect(assignmentDao.getById(assignment.getId())).andReturn(assignment);

        replayMocks();

        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, params);
        assertRolesAllowed(actualAuthorizations, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    //todo, what actually is tested is the error message in the log. we don't have a good way to capture it.
    //todo, leaving test and will add espected login message, once we have this functionality.
    public void testAuthorizedRolesWithErroLog() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        String[] assignmentId = {"15"};
        params.put("assignment", assignmentId);
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, params);
        assertRolesAllowed(actualAuthorizations, STUDY_SUBJECT_CALENDAR_MANAGER);
    }
    

    public void testSubjectAssignedOnSubmit() throws Exception {
        expect(command.takeSubjectOffStudy()).andReturn(assignment);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToSchedule", mv.getViewName());
        assertEquals("Calendar Parameter wrong", "20", mv.getModelMap().get("calendar"));
    }

    public void testBindDate() throws Exception {
        request.addParameter("expectedEndDate", "08/05/2003");
        expect(command.takeSubjectOffStudy()).andReturn(assignment);
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();

        assertDayOfDate(2003, Calendar.AUGUST, 5, command.convertStringToDate(command.getExpectedEndDate()));
    }


    public void testBindStudySubjectAssignment() throws Exception {
        request.setParameter("assignment", "10");
        expect(assignmentDao.getById(10)).andReturn(assignment);
        expect(command.takeSubjectOffStudy()).andReturn(assignment);
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Subject assignments are different", assignment.getId(), command.getAssignment().getId());
    }
}
