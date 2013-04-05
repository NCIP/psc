/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class ImportActivitiesControllerTest extends ControllerTestCase {
    private ImportActivitiesController controller;
    private ImportActivitiesCommand command;
    List<Activity> activities;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;
    private static final String TEST_XML = "<sources><source=\"ts\"/></sources>";
    private MockMultipartHttpServletRequest multipartRequest;
    private Source source;
    private List<Source> sources;
    private List<ActivityType> activityTypes;

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
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        PlannedActivityDao plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        ActivityDao activityDao = registerDaoMockFor(ActivityDao.class);
        source = setId(11, createNamedInstance("Test Source", Source.class));
        controller.setSourceDao(sourceDao);

        sources = new ArrayList<Source>();
        activityTypes = new ArrayList<ActivityType>();
        sources.add(source);
        multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setMethod("POST");
        multipartRequest.setSession(session);
        multipartRequest.addParameter("activitySource", source.getId().toString());
        expect(sourceDao.getById(source.getId())).andReturn(source).anyTimes();
        List<Activity> activities = new ArrayList<Activity>();
        expect(activityDao.getBySourceId(source.getId())).andReturn(activities).anyTimes();
    }
    
    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
    }

    public void testSubmit() throws Exception {
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        expect(sourceDao.getAll()).andReturn(sources).anyTimes();
        assertEquals("Wrong view", "redirectToActivities", getOnSubmitData().getViewName());
    }

    public void testSubmitWithReturnToActivity() throws Exception {
        expect(sourceDao.getAll()).andReturn(sources).anyTimes();
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        ModelAndView mv = getOnSubmitData();

        assertEquals("Wrong view", "redirectToActivities", mv.getViewName());
    }

    public void testGet() throws Exception {
        multipartRequest.setMethod("GET");

        ModelAndView mv = controller.handleRequest(multipartRequest, response);

        assertNotNull("View is null", mv.getViewName());
    }

    public void testBindActivitiesXml() throws Exception {
//        List<Source> sources = new ArrayList<Source>();
        expect(sourceDao.getAll()).andReturn(sources).anyTimes();
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        MultipartFile mockFile = new MockMultipartFile("activitiesFile", TEST_XML.getBytes());
        multipartRequest.addFile(mockFile);

        expect(command.apply()).andReturn(source).anyTimes();
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
       expect(command.apply()).andReturn(source).anyTimes();
    }
}
