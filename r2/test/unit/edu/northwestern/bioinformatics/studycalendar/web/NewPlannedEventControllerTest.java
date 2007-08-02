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
    private Period period;
    
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);
        activityDao = registerMockFor(ActivityDao.class);
        period = registerMockFor(Period.class);
        controller.setPeriodDao(periodDao);
        controller.setActivityDao(activityDao);
    }
        
    public void testReferenceData() throws Exception { 	
        List<Activity> theList = new ArrayList<Activity>();
        expect(activityDao.getAll()).andReturn(theList);
    	expect(periodDao.getById(42)).andReturn(period);
    	expect(period.getEndDay()).andReturn(9);
    	expect(period.getId()).andReturn(42);
        replayMocks();	
    	
    	request.setMethod("GET");
        request.addParameter("id", "42");
        ModelAndView mv = controller.handleRequest(request, response);
        
        assertEquals((Integer)42, (Integer)((Period) mv.getModel().get("period")).getId());
        verifyMocks();
    }

}