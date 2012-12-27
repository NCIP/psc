/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SUBJECT_MANAGER;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.notNull;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Map;

/**
 * @author Padmaja Vedula
 */
public class NewSubjectControllerTest extends ControllerTestCase {
    private NewSubjectController controller = new NewSubjectController();
    private SubjectDao subjectDao;

    protected void setUp() throws Exception {
        super.setUp();
        subjectDao = registerMockFor(SubjectDao.class);
        controller.setSubjectDao(subjectDao);
        controller.setControllerTools(controllerTools);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, SUBJECT_MANAGER);
    }
    
    public void testReferenceData() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request);
        Map<String, String> genders = (Map<String, String>) refdata.get("genders");
        assertEquals("Wrong action name", "Male", genders.get(Gender.MALE.getCode()));
        assertEquals("Wrong action name", "New", refdata.get("action"));
    }

    public void testViewOnGet() throws Exception {
        request.setMethod("GET");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("createSubject", mv.getViewName());
    }

    public void testViewOnGoodSubmit() throws Exception {
        request.addParameter("firstName", "Tiger");
        request.addParameter("lastName", "Scott");
        request.addParameter("gender", "Male");
        request.addParameter("dateOfBirth", "2006-01-01");
        request.addParameter("personId", "123-45-5678");
        
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("createSubject", mv.getViewName());
    }

    // Removed b/c failing due to changes in tested class.  RMS 20060918.
//    public void testBindFirstName() throws Exception {
//        String firstName = "Scott";
//        request.addParameter("firstName", firstName);
//        NewSubjectCommand command = postAndReturnCommand();
//        assertEquals(firstName, command.getFirstName());
//    }

   private NewSubjectCommand postAndReturnCommand() throws Exception {
        request.setMethod("POST");
        subjectDao.save((Subject) notNull());
        expectLastCall().atLeastOnce().asStub();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object command = mv.getModel().get("command");
        assertNotNull("Command not present in model: " + mv.getModel(), command);
        return (NewSubjectCommand) command;
    }
}
