package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import org.easymock.classextension.EasyMock;
import org.restlet.data.Status;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivitiesResourceTest extends AuthorizedResourceTestCase<PlannedActivitiesResource> {
    private static final String STUDY_NAME = "ENU-1454";
    private static final String EPOCH_NAME = "Treatment";
    private static final String SEGMENT_NAME = "Baker";
    private static final String PERIOD_GRID_ID = "PERIOD-UID";
    private static final Integer PERIOD_ID = 14;

    private static final String ACTIVITY_CODE = "F";
    private static final Activity ACTIVITY
        = Fixtures.createActivity("Fool", ACTIVITY_CODE,
            Fixtures.DEFAULT_ACTIVITY_SOURCE, Fixtures.DEFAULT_ACTIVITY_TYPE);
    private static final String ACTIVITY_SOURCE_NAME
        = Fixtures.DEFAULT_ACTIVITY_SOURCE.getName();
    private static final Integer DAY = 7;
    private static final Population POPULATION = Fixtures.createPopulation("T", "Tea");
    private Set<Population> populations = new HashSet<Population>();
    private static final Integer WEIGHT = 8;

    private AmendedTemplateHelper helper;
    private AmendmentService amendmentService;
    private ActivityDao activityDao;
    private PopulationDao populationDao;

    private Study study;
    private Period revisedPeriod;
    private Amendment devAmendment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = registerMockFor(AmendedTemplateHelper.class);
        helper.setRequest(request);
        EasyMock.expectLastCall().atLeastOnce();
        
        amendmentService = registerMockFor(AmendmentService.class);
        activityDao = registerMockFor(ActivityDao.class);
        populationDao = registerMockFor(PopulationDao.class);

        study = createBasicTemplate();
        study.setAssignedIdentifier(STUDY_NAME);
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        epoch.setName(EPOCH_NAME);
        StudySegment segment = epoch.getStudySegments().get(1);
        segment.setName(SEGMENT_NAME);
        assignIds(study);
        populations.add(POPULATION);
        study.setPopulations(populations);
        expect(helper.getAmendedTemplateOrNull()).andStubReturn(study);

        Period period = setId(PERIOD_ID, createPeriod("Z", 4, 8, 12));
        period.setGridId(PERIOD_GRID_ID);

        devAmendment = new Amendment();
        study.setDevelopmentAmendment(devAmendment);
        getTestingDeltaService().updateRevision(devAmendment, segment, Add.create(period));

        UriTemplateParameters.STUDY_IDENTIFIER.putIn(request, STUDY_NAME);
        UriTemplateParameters.EPOCH_NAME.putIn(request, epoch.getName());
        UriTemplateParameters.STUDY_SEGMENT_NAME.putIn(request, segment.getName());
        UriTemplateParameters.PERIOD_IDENTIFIER.putIn(request, period.getName());
    }

    @Override
    protected PlannedActivitiesResource createAuthorizedResource() {
        PlannedActivitiesResource res = new PlannedActivitiesResource();
        res.setAmendedTemplateHelper(helper);
        res.setTemplateService(new TestingTemplateService());
        res.setAmendmentService(amendmentService);
        res.setActivityDao(activityDao);
        res.setPopulationDao(populationDao);
        return res;
    }

    public void test404WhenPeriodCannotBeLocated() throws Exception {
        expect(helper.drillDown(Period.class)).
            andThrow(new AmendedTemplateHelper.NotFound("No such thing in there"));

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("No such thing in there");
    }
    
    public void testPostPreventedUnlessInDevelopment() throws Exception {
        expectSuccessfulDrillDown();
        expect(helper.isDevelopmentRequest()).andReturn(false);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertEntityTextContains("You can only add new planned activities to the development version of the template");
    }

    public void testAddPlannedActivityWithMinimumAttributes() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectFindActivity();

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY, WEIGHT);
        expectPlannedActivityAdd(expectedPlannedActivity);

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
    }

    public void testAddPlannedActivityWhenActivityDoesNotExist() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_CODE, ACTIVITY_SOURCE_NAME)).
            andReturn(null);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Activity not found");
    }

    public void testAddPlannedActivityWithDetailsAndCondition() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectFindActivity();
        expectRequestEntityFormAttribute("details", "foom");
        expectRequestEntityFormAttribute("condition", "foom > 12");

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY, WEIGHT);
        expectedPlannedActivity.setDetails("foom");
        expectedPlannedActivity.setCondition("foom > 12");
        expectPlannedActivityAdd(expectedPlannedActivity);

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
    }

    public void testAddPlannedActivityWithPopulation() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectFindActivity();
        expectRequestEntityFormAttribute("population", "T");

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY, WEIGHT);
        expectedPlannedActivity.setPopulation(POPULATION);
        expectPlannedActivityAdd(expectedPlannedActivity);

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
    }

    public void testAddPlannedActivityWithPopulationWhenPopulationDoesNotExist() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectRequestEntityFormAttribute("population", "C");
        expectFindActivity();

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Population not found");
    }

    public void testAddPlannedActivityRequiresDay() throws Exception {
        expectPostMayProceed();
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Missing required parameter day");
    }

    public void testAddPlannedActivityDayMustBeInteger() throws Exception {
        expectPostMayProceed();
        expectRequestEntityFormAttribute("day", "twelve");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Parameter day must be an integer ('twelve' isn't)");
    }

    public void testAddPlannedActivityRequiresActivitySource() throws Exception {
        expectPostMayProceed();
        expectRequestEntityFormAttribute("day", DAY.toString());
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Missing required parameter activity-source");
    }

    public void testAddPlannedActivityRequiresActivityCode() throws Exception {
        expectPostMayProceed();
        expectRequestEntityFormAttribute("day", DAY.toString());
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Missing required parameter activity-code");
    }

    public void testAddPlannedActivityFailsWithSCValidationExceptionFromDomainLayer() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectFindActivity();

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY, WEIGHT);
        expect(amendmentService.addPlannedActivityToDevelopmentAmendmentAndSave(revisedPeriod, expectedPlannedActivity))
            .andThrow(new StudyCalendarValidationException("I have some bad news"));

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("I have some bad news");
    }

    public void testAddPlannedActivityConvertsHtmlEntitiesToText() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectFindActivity();
        expectRequestEntityFormAttribute("details", "Four &amp; five are outr&eacute;");
        expectRequestEntityFormAttribute("condition", "x &lt; 4 or y &gt; 5");

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY, WEIGHT);
        expectedPlannedActivity.setDetails("Four & five are outré");
        expectedPlannedActivity.setCondition("x < 4 or y > 5");

        expectPlannedActivityAdd(expectedPlannedActivity);

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
    }

    ////// EXPECTATIONS

    private void expectPlannedActivityAdd(PlannedActivity expectedPlannedActivity) {
        expect(amendmentService.addPlannedActivityToDevelopmentAmendmentAndSave(revisedPeriod,
            expectedPlannedActivity)).andReturn(study);
    }

    private void expectSuccessfulDrillDown() {
        Study revisedStudy = revise(study, devAmendment);
        revisedPeriod = revisedStudy.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1).getPeriods().first();
        expect(helper.drillDown(Period.class)).andReturn(revisedPeriod);
    }

    private void expectIsDevelopmentRequest() {
        expect(helper.isDevelopmentRequest()).andReturn(true);
    }

    private void expectPostMayProceed() {
        expectSuccessfulDrillDown();
        expectIsDevelopmentRequest();
    }

    private void expectFindActivity() {
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_CODE, ACTIVITY_SOURCE_NAME)).
            andReturn(ACTIVITY);
    }

    private void expectMinimumPostAttributes() throws IOException {
        expectRequestEntityFormAttribute("day", DAY.toString());
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);
        expectRequestEntityFormAttribute("weight", WEIGHT.toString());
    }
}
