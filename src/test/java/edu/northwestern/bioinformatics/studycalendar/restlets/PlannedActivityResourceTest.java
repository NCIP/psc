package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import static org.easymock.EasyMock.*;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivityResourceTest extends ResourceTestCase<PlannedActivityResource> {
    private AmendmentService amendmentService;
    private AmendedTemplateHelper helper;
    private ActivityDao activityDao;
    private PopulationDao populationDao;
    private PlannedActivityDao plannedActivityDao;

    private PlannedActivity plannedActivity;
    private Activity activity;
    private Population population;
    private Study study;
    private Period period;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate();
        period = createPeriod(4, 5, 2);
        plannedActivity = createPlannedActivity("Any", 4);
        period.addPlannedActivity(plannedActivity);
        activity = plannedActivity.getActivity();
        population = createPopulation("T", "Squares");

        helper = registerMockFor(AmendedTemplateHelper.class);
        helper.setRequest(request);
        expectLastCall().atLeastOnce();
        expect(helper.getRealStudy()).andReturn(study).anyTimes();
        amendmentService = registerMockFor(AmendmentService.class);
        activityDao = registerMockFor(ActivityDao.class);
        populationDao = registerMockFor(PopulationDao.class);
        plannedActivityDao = registerMockFor(PlannedActivityDao.class);
    }

    @Override
    protected PlannedActivityResource createResource() {
        PlannedActivityResource res = new PlannedActivityResource();
        res.setAmendedTemplateHelper(helper);
        res.setAmendmentService(amendmentService);
        res.setActivityDao(activityDao);
        res.setPopulationDao(populationDao);
        res.setPlannedActivityDao(plannedActivityDao);
        res.setTemplateService(new TestingTemplateService());
        return res;
    }

    public void testEnabledMethods() throws Exception {
        expectSuccessfulDrillDown();
        replayMocks();
        assertAllowedMethods("PUT", "DELETE");
    }

    public void testPutAllowedForStudyCoordinator() throws Exception {
        expectSuccessfulDrillDown();
        doInit();
        assertEquals(1, getResource().authorizedRoles(Method.PUT).size());
        assertEquals(Role.STUDY_COORDINATOR, getResource().authorizedRoles(Method.PUT).iterator().next());
    }

    public void testDeleteAllowedForStudyCoordinator() throws Exception {
        expectSuccessfulDrillDown();
        doInit();
        assertEquals(1, getResource().authorizedRoles(Method.DELETE).size());
        assertEquals(Role.STUDY_COORDINATOR, getResource().authorizedRoles(Method.DELETE).iterator().next());
    }

    public void testPutPreventedUnlessInDevelopment() throws Exception {
        expectMinimumPutEntity();
        expectSuccessfulDrillDown();
        expect(helper.isDevelopmentRequest()).andReturn(false);

        doPut();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertEntityTextContains("You can only update planned activities in the development version of the template");
    }

    public void testPutCreatesANewPlannedActivityIfNotPresent() throws Exception {
        String expectedIdent = "unique-in-all-the-world";
        UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.putIn(request, expectedIdent);
        expect(helper.drillDown(PlannedActivity.class)).andThrow(new AmendedTemplateHelper.NotFound("No such thing"));
        expect(helper.drillDown(Period.class)).andReturn(period);
        expectIsDevelopmentRequest();

        expectMinimumPutEntity();
        expectFindActivityByCodeAndSource();
        expect(plannedActivityDao.getByGridId(expectedIdent)).andReturn(null);

        PlannedActivity expectedPlannedActivity = setGridId(expectedIdent, createPlannedActivity(activity, 6));

        amendmentService.updateDevelopmentAmendment(period, Add.create(expectedPlannedActivity));

        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals(MediaType.APPLICATION_WWW_FORM, response.getEntity().getMediaType());
        assertEntityTextContains("day=6");
    }

    public void testPutFailsIfPlannedActivityExistsButDoesNotMatchTheRemainderOfTheUrl() throws Exception {
        String expectedIdent = "unique-in-all-the-world";
        UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.putIn(request, expectedIdent);
        expect(helper.drillDown(PlannedActivity.class)).andThrow(
            new AmendedTemplateHelper.NotFound("No such thing"));
        expectIsDevelopmentRequest();

        expectMinimumPutEntity();
        expect(plannedActivityDao.getByGridId(expectedIdent)).andReturn(new PlannedActivity());

        doPut();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertEntityTextContains(
            "The planned activity unique-in-all-the-world exists, but is not part of the designated study");
    }

    public void testPutUpdatesAllPlannedActivityProperties() throws Exception {
        expectWriteOperationMayProceed();
        expectMinimumPutEntity();
        expectRequestEntityFormAttribute("details", "sharp");
        expectRequestEntityFormAttribute("condition", "enough");
        expectRequestEntityFormAttribute("population", population.getNaturalKey());
        expectFindActivityByCodeAndSource();
        expectFindPopulation();

        amendmentService.updateDevelopmentAmendment(plannedActivity, PropertyChange.create("day", 4, 6));
        amendmentService.updateDevelopmentAmendment(plannedActivity,
            PropertyChange.create("activity", activity, activity));
        amendmentService.updateDevelopmentAmendment(plannedActivity, PropertyChange.create("details", null, "sharp"));
        amendmentService.updateDevelopmentAmendment(plannedActivity, PropertyChange.create("condition", null, "enough"));
        amendmentService.updateDevelopmentAmendment(plannedActivity,
            PropertyChange.create("population", null, population));

        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
        assertEntityTextContains("day=6");
    }

    public void testDelete404sWhenPlannedActivityNotFound() throws Exception {
        expect(helper.drillDown(PlannedActivity.class)).
            andThrow(new AmendedTemplateHelper.NotFound("No such thing in there"));

        doDelete();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("No such thing in there");
    }

    public void testDeletePreventedUnlessInDevelopment() throws Exception {
        expectSuccessfulDrillDown();
        expect(helper.isDevelopmentRequest()).andReturn(false);

        doDelete();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertEntityTextContains("You can only delete planned activities from the development version of the template");
    }

    public void testDelete() throws Exception {
        expect(helper.drillDown(PlannedActivity.class)).andReturn(plannedActivity);
        expectIsDevelopmentRequest();

        amendmentService.updateDevelopmentAmendment(period, Remove.create(plannedActivity));

        doDelete();

        assertResponseStatus(Status.SUCCESS_NO_CONTENT);
    }

    private void expectMinimumPutEntity() throws IOException {
        expectRequestEntityFormAttribute("day", "6");
        expectRequestEntityFormAttribute("activity-code", activity.getCode());
        expectRequestEntityFormAttribute("activity-source", activity.getSource().getNaturalKey());
    }

    private void expectFindActivityByCodeAndSource() {
        expect(activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getNaturalKey())).andReturn(activity);
    }

    private void expectFindPopulation() {
        expect(populationDao.getByAbbreviation(study, population.getAbbreviation())).andReturn(population);
    }

    ////// EXPECTATIONS

    private void expectSuccessfulDrillDown() {
        expect(helper.drillDown(PlannedActivity.class)).andReturn(plannedActivity);
    }

    private void expectIsDevelopmentRequest() {
        expect(helper.isDevelopmentRequest()).andReturn(true);
    }

    private void expectWriteOperationMayProceed() {
        expectSuccessfulDrillDown();
        expectIsDevelopmentRequest();
    }
}
