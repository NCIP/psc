package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

public class ScheduleReconsentControllerTest extends ControllerTestCase {

    private StudyDao studyDao;
    private NowFactory nowFactory;
    private StudyService studyService;

    private ScheduleReconsentCommand command;
    private ScheduleReconsentController controller;


    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        nowFactory = registerMockFor(NowFactory.class);
        studyService = registerMockFor(StudyService.class);
        command = registerMockFor(ScheduleReconsentCommand.class, ScheduleReconsentCommand.class.getMethod("apply"));
        command.setNowFactory(nowFactory);


        controller = new ScheduleReconsentController(){
           @Override protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudyDao(studyDao);
        controller.setStudyService(studyService);
        controller.setNowFactory(nowFactory);
    }

    public void testBindDate() throws Exception {
        request.addParameter("startDate", "08/05/2003");

        executeRequest();

        assertDayOfDate(2003, Calendar.AUGUST, 5, command.getStartDate());
    }

    private ModelAndView executeRequest() throws Exception {
        expect(nowFactory.getNow()).andReturn(DateTools.createDate(2003, Calendar.AUGUST, 2));
        command.apply();
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        //BindingResult result = (BindingResult) mv.getModel().get(BindingResult.MODEL_KEY_PREFIX + "command");
        //assertEquals("There were errors in the request: " + result.getAllErrors(), 0, result.getErrorCount());
        return mv;
    }
}
