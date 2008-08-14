package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import static org.easymock.EasyMock.expect;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;

public class ImportActivitiesControllerTest extends ControllerTestCase {
    private ImportActivitiesController controller;
    private ImportActivitiesCommand command;
    List<Activity> activities;
    private SourceDao sourceDao;
    private static final String TEST_XML = "<sources><source=\"ts\"/></sources>";
    private MockMultipartHttpServletRequest multipartRequest;
    private Source source;
    private List<Source> sources;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        command = registerMockFor(ImportActivitiesCommand.class, ImportActivitiesCommand.class.getMethod("apply"));
        controller = new ImportActivitiesController() {

            protected ImportActivitiesCommand formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };

        // Stop controller from calling validation
        controller.setValidators(null);
        sourceDao = registerDaoMockFor(SourceDao.class);
        PlannedActivityDao plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        ActivityDao activityDao = registerDaoMockFor(ActivityDao.class);
        source = setId(11, createNamedInstance("Test Source", Source.class));
        controller.setSourceDao(sourceDao);
        controller.setActivityDao(activityDao);
        controller.setPlannedActivityDao(plannedActivityDao);

        sources = new ArrayList<Source>();
        sources.add(source);
//        command.setSourceId(source.getId());
        multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setMethod("POST");
        multipartRequest.setSession(session);
        multipartRequest.addParameter("activitySource", source.getId().toString());
        expect(sourceDao.getById(source.getId())).andReturn(source).anyTimes();
        List<Activity> activities = new ArrayList<Activity>();

//        List<Source> sourcesAfterAdding = new ArrayList<Source>();
//        expect(sourceDao.getAll()).andReturn(sourcesAfterAdding).anyTimes();
        expect(activityDao.getBySourceId(source.getId())).andReturn(activities).anyTimes();
    }

    public void testSubmit() throws Exception {
//        List<Source> sources = new ArrayList<Source>();
//        sources.add(source);
        expect(sourceDao.getAll()).andReturn(sources).anyTimes();
        assertEquals("Wrong view", "activity", getOnSubmitData().getViewName());
    }

    public void testSubmitWithReturnToActivity() throws Exception {
//        List<Source> sources = new ArrayList<Source>();
        expect(sourceDao.getAll()).andReturn(sources).anyTimes();

        ModelAndView mv = getOnSubmitData();

        assertEquals("Wrong view", "activity", mv.getViewName());
    }

    public void testGet() throws Exception {
        multipartRequest.setMethod("GET");

        ModelAndView mv = controller.handleRequest(multipartRequest, response);

        assertNotNull("View is null", mv.getViewName());
    }

    public void testBindActivitiesXml() throws Exception {
//        List<Source> sources = new ArrayList<Source>();
        expect(sourceDao.getAll()).andReturn(sources).anyTimes();
        MultipartFile mockFile = new MockMultipartFile("activitiesFile", TEST_XML.getBytes());
        multipartRequest.addFile(mockFile);

        command.apply();
        replayMocks();

        ModelAndView mv = controller.handleRequest(multipartRequest, response);
        assertNoBindingErrorsFor("activitiesFile", mv.getModel());
        verifyMocks();


        assertNotNull("Activities file should not be null", command.getActivitiesFile());
    }

    protected ModelAndView getOnSubmitData() throws Exception {
        onSubmitDataCalls();
        replayMocks();

        ModelAndView mv = controller.handleRequest(multipartRequest, response);
        verifyMocks();
        return mv;
    }

    protected void onSubmitDataCalls() throws Exception{
        command.apply();
    }
}
