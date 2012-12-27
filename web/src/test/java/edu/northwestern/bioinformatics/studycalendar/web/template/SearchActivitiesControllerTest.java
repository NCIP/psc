/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static org.easymock.EasyMock.expect;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;

/**
 * @author John Dzak
 */
public class SearchActivitiesControllerTest extends ControllerTestCase {
    private SearchActivitiesController controller;
    private SearchActivitiesCommand command;
    private List<Activity> activities;
    private ActivityDao activityDao;
    private Source source0, source1;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;
    private List<Activity> activityResult;

    ActivityType activityType1, activityType2;

    protected void setUp() throws Exception {
        super.setUp();

        command = registerMockFor(SearchActivitiesCommand.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);

        controller = new SearchActivitiesController() {
            @Override
            protected SearchActivitiesCommand getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setControllerTools(controllerTools);
        controller.setActivityDao(activityDao);
        controller.setSourceDao(sourceDao);
        controller.setActivityTypeDao(activityTypeDao);

        source0 = setId(0, createNamedInstance("PSC Manual Entry Source", Source.class));
        source1 = setId(1, createNamedInstance("LOINK", Source.class));
        activityType1 = createActivityType("INTERVENTION");
        activityType2 = createActivityType("DISEASE_MEASURE");
        activities = asList(
                createActivity("Activity A", "AA", source0, activityType1),
                createActivity("Activity B", "BB", source1, activityType2)
        );

        activityResult = new ArrayList<Activity>();

        request.setMethod("GET");
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations,
            STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    public void testHandle() throws Exception {
        String searchText = "";
        expectSearch(searchText, null, null);
        expectRefDataCalls("activities", searchText);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/activities", mv.getViewName());
    }

    public void testHandleSearchByFullName() throws Exception {
        String searchText = "Activity B";
        expectSearch(searchText, null, null);

        List<Activity> actual = new ArrayList<Activity>();
        actual.add(activities.get(1));

        expect(activityDao.getActivitiesBySearchText(searchText)).andReturn(actual);
        assertEquals("Wrong search results size", 1, actual.size());
        assertEquals("Wrong search results", "Activity B", actual.get(0).getName());
    }

    public void testHandleSearchByFullNameLowerCase() throws Exception {
        String searchText = "activity b";
        expectSearch(searchText, null, null);

        List<Activity> actual = (List<Activity>) getRefData("activities", searchText );

        assertEquals("Wrong search results size", 1, actual.size());
        assertEquals("Wrong search results", "Activity B", actual.get(0).getName());
    }

    public void testHandleSearchByPartialName() throws Exception {
        String searchText = "Activity";
        expectSearch(searchText, null, null);

        List<Activity> actual = (List<Activity>) getRefData("activities", searchText);

        assertEquals("Wrong search results size", 2, actual.size());
        assertEquals("Wrong search results", "Activity A", actual.get(0).getName());
        assertEquals("Wrong search results", "Activity B", actual.get(1).getName());
    }

    public void testHandleSearchByCode() throws Exception {
        String searchText = "AA";
        expectSearch(searchText, null, null);

        List<Activity> actual = (List<Activity>) getRefData("activities", searchText);

        assertEquals("Wrong search results size", 1, actual.size());
        assertEquals("Wrong search results", "AA", actual.get(0).getCode());
    }

    public void testHandleSearchByCodeLowerCase() throws Exception {
        String searchText = "bb";
        expectSearch(searchText, null, null);

        List<Activity> actual = (List<Activity>) getRefData("activities", searchText);

        assertEquals("Wrong search results size", 1, actual.size());
        assertEquals("Wrong search results", "BB", actual.get(0).getCode());
    }

    public void testHandleFilterBySource() throws Exception {
        String searchText = "Activity";
        expectSearch(searchText, source0, null);

        List<Activity> actual = (List<Activity>) getRefData("activities", searchText);

        assertEquals("Wrong search results size", 1, actual.size());
        assertEquals("Wrong search results", "Activity A", actual.get(0).getName());
    }

    public void testHandleFilterByActivityType() throws Exception {
        String searchText = "Activity";
        expectSearch(searchText, null, activityType2);

        List<Activity> actual = (List<Activity>) getRefData("activities", searchText);

        assertEquals("Wrong search results size", 1, actual.size());
        assertEquals("Wrong search results", "Activity B", actual.get(0).getName());
    }

    public void testHandleGetIsError() throws Exception {
        request.setMethod("POST");
        replayMocks();

        assertNull(controller.handleRequest(request, response));
        verifyMocks();

        assertEquals("Wrong HTTP status code", HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Wrong error message", "GET is the only valid method for this URL", response.getErrorMessage());
    }

    private void expectSearch(String searchText, Source source, ActivityType activityType) {
        expect(command.getSearchText()).andReturn(searchText).anyTimes();
        expect(command.getSource()).andReturn(source);
        expect(command.getActivityType()).andReturn(activityType);
    }

    public void expectRefDataCalls(String searchText, String searchActivityText) {
        for (Activity activity: activities) {
            if (activity.getName().toLowerCase().contains(searchActivityText.toLowerCase()) || activity.getCode().toLowerCase().contains(searchActivityText.toLowerCase())) {
                activityResult.add(activity);
            }
        }
        expect(activityDao.getActivitiesBySearchText(searchActivityText)).andReturn(activityResult);
    }

    public Object getRefData(String refdataKey, String searchActivityText) throws Exception{
        expectRefDataCalls(refdataKey, searchActivityText);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        return mv.getModel().get(refdataKey);
    }
}
