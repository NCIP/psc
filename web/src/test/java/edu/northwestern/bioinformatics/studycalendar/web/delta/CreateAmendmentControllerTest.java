package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;

import java.util.List;
import java.util.Date;
import java.util.Calendar;

/**
 * @author Saurabh Agrawal
 * @crated Feb 3, 2009
 */
public class CreateAmendmentControllerTest extends ControllerTestCase {

    private StudyDao studyDao;
    private AmendmentDao amendmentDao;

    private CreateAmendmentController controller;

    private StudyService studyService;
    protected Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new CreateAmendmentController();
        studyService = registerMockFor(StudyService.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        controller.setAmendmentDao(amendmentDao);
        controller.setStudyDao(studyDao);
        controller.setStudyService(studyService);
        controller.setControllerTools(controllerTools);
        study = new Study();
    }


    public void testSaveValidAmendment() throws Exception {
        request.addParameter("date", "01/01/2009");
        request.addParameter("study", "1");
        expect(studyDao.getById(1)).andReturn(study);
        expect(amendmentDao.getByDateNameStudy(createDate(2009, Calendar.JANUARY, 01, 0, 0, 0), null, study)).andReturn(null);
        studyService.save(study);
        replayMocks();
        ModelAndView modelAndView = controller.handleRequest(request, response);
        verifyMocks();

    }

    public void testMissingAmendmentDate() throws Exception {

        request.addParameter("study", "1");
        expect(studyDao.getById(1)).andReturn(study);
        replayMocks();
        ModelAndView modelAndView = controller.handleRequest(request, response);
        verifyMocks();

        BindingResult result = (BindingResult) modelAndView.getModel().get(BindingResult.MODEL_KEY_PREFIX + "command");
        List<FieldError> allErrors = result.getAllErrors();
        assertEquals("unexpected errors : " + allErrors, 1, allErrors.size());


    }
}