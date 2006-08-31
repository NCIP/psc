package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;

/**
 * @author Padmaja Vedula
 */
public class NewParticipantControllerTest extends ControllerTestCase {
    private NewParticipantController controller = new NewParticipantController();
    private ParticipantDao participantDao;

    protected void setUp() throws Exception {
        super.setUp();
        participantDao = registerMockFor(ParticipantDao.class);
        controller.setParticipantDao(participantDao);
    }

    public void testReferenceData() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request);
        HashMap<String, String> genders = (HashMap) refdata.get("genders");
        assertEquals("Wrong action name", "Male", genders.get("Male"));
        assertEquals("Wrong action name", "New", refdata.get("action"));
    }

    public void testViewOnGet() throws Exception {
        request.setMethod("GET");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("createParticipant", mv.getViewName());
    }

    public void testViewOnGoodSubmit() throws Exception {
        request.addParameter("firstName", "Tiger");
        request.addParameter("lastName", "Scott");
        request.addParameter("gender", "Male");
        request.addParameter("dateOfBirth", "2006-01-01");
        request.addParameter("personId", "123-45-5678");
        
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("createParticipant", mv.getViewName());
    }

    public void testBindFirstName() throws Exception {
        String firstName = "Scott";
        request.addParameter("firstName", firstName);
        NewParticipantCommand command = postAndReturnCommand();
        assertEquals(firstName, command.getFirstName());
    }

   private NewParticipantCommand postAndReturnCommand() throws Exception {
        request.setMethod("POST");
        participantDao.save((Participant) notNull());  
        expectLastCall().atLeastOnce().asStub();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object command = mv.getModel().get("command");
        assertNotNull("Command not present in model: " + mv.getModel(), command);
        return (NewParticipantCommand) command;
    }
}
