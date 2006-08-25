package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

/**
 * @author Jaron Sampson
 */
public class NewPlannedEventControllerTest extends ControllerTestCase {
    private NewPlannedEventController controller = new NewPlannedEventController();
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);
        activityDao = registerMockFor(ActivityDao.class);
        controller.setPeriodDao(periodDao);
        controller.setActivityDao(activityDao);
    }
    
    //TODO: test for the period id parameter

    //TODO: test reference data
    

    /*
    public void testModelAndView() throws Exception {
    	List<Activity> theList = new ArrayList<Activity>();
        expect(activityDao.getAll()).andReturn(theList);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertSame("Activities list missing or wrong", theList, mv.getModel().get("activities"));

    }
    */   
    
    /*
    private NewPlannedEventCommand postAndReturnCommand() throws Exception {
        request.setMethod("POST");
        periodDao.save((Period) notNull());  // TODO: once there is validation, this won't happen
        expectLastCall().atLeastOnce().asStub();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object command = mv.getModel().get("command");
        assertNotNull("Command not present in model: " + mv.getModel(), command);
        return (NewPlannedEventCommand) command;
    }
*/
}