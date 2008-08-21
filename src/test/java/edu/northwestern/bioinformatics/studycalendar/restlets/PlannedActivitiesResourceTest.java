package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

import java.io.IOException;
import java.net.URLEncoder;

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

    private AmendedTemplateHelper helper;
    private AmendmentService amendmentService;
    private StudyService studyService;
    private ActivityDao activityDao;
    private PopulationDao populationDao;
    private PeriodDao periodDao;

    private Study study, revisedStudy;
    private Period period, revisedPeriod;
    private Amendment devAmendment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = registerMockFor(AmendedTemplateHelper.class);
        helper.setRequest(request);
        EasyMock.expectLastCall().atLeastOnce();
        
        amendmentService = registerMockFor(AmendmentService.class);
        studyService = registerMockFor(StudyService.class);
        activityDao = registerMockFor(ActivityDao.class);
        populationDao = registerMockFor(PopulationDao.class);
        periodDao = registerMockFor(PeriodDao.class);

        study = createBasicTemplate();
        study.setName(STUDY_NAME);
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(1);
        epoch.setName(EPOCH_NAME);
        StudySegment segment = epoch.getStudySegments().get(1);
        segment.setName(SEGMENT_NAME);
        assignIds(study);

        period = setId(PERIOD_ID, createPeriod("Z", 4, 8, 12));
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
    protected PlannedActivitiesResource createResource() {
        PlannedActivitiesResource res = new PlannedActivitiesResource();
        res.setAmendedTemplateHelper(helper);
        res.setTemplateService(new TestingTemplateService());
        res.setStudyService(studyService);
        res.setAmendmentService(amendmentService);
        res.setActivityDao(activityDao);
        res.setPopulationDao(populationDao);
        res.setPeriodDao(periodDao);
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

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY);
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
        expectEntityFormAttribute("details", "foom");
        expectEntityFormAttribute("condition", "foom > 12");

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY);
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
        expectEntityFormAttribute("population", "T");
        expect(populationDao.getByAbbreviation(revisedStudy, "T")).andReturn(POPULATION);

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY);
        expectedPlannedActivity.setPopulation(POPULATION);
        expectPlannedActivityAdd(expectedPlannedActivity);

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
    }

    public void testAddPlannedActivityWithPopulationWhenPopulationDoesNotExist() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectEntityFormAttribute("population", "T");
        expectFindActivity();
        expect(populationDao.getByAbbreviation(revisedStudy, "T")).andReturn(null);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Population not found");
    }

    public void testAddPlannedActivityRequiresDay() throws Exception {
        expectPostMayProceed();
        expectEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectEntityFormAttribute("activity-code", ACTIVITY_CODE);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Missing required parameter day");
    }

    public void testAddPlannedActivityDayMustBeInteger() throws Exception {
        expectPostMayProceed();
        expectEntityFormAttribute("day", "twelve");
        expectEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectEntityFormAttribute("activity-code", ACTIVITY_CODE);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Parameter day must be an integer ('twelve' isn't)");
    }

    public void testAddPlannedActivityRequiresActivitySource() throws Exception {
        expectPostMayProceed();
        expectEntityFormAttribute("day", DAY.toString());
        expectEntityFormAttribute("activity-code", ACTIVITY_CODE);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Missing required parameter activity-source");
    }

    public void testAddPlannedActivityRequiresActivityCode() throws Exception {
        expectPostMayProceed();
        expectEntityFormAttribute("day", DAY.toString());
        expectEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("Missing required parameter activity-code");
    }

    public void testAddPlannedActivityFailsWithSCValidationExceptionFromDomainLayer() throws Exception {
        expectPostMayProceed();
        expectMinimumPostAttributes();
        expectFindActivity();

        PlannedActivity expectedPlannedActivity = createPlannedActivity(ACTIVITY, DAY);
        expect(periodDao.getByGridId(revisedPeriod)).andReturn(period);
        amendmentService.updateDevelopmentAmendment(period, Add.create(expectedPlannedActivity));
        expectLastCall().andThrow(new StudyCalendarValidationException("I have some bad news"));

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEntityTextContains("I have some bad news");
    }

    ////// EXPECTATIONS

    private void expectPlannedActivityAdd(PlannedActivity expectedPlannedActivity) {
        expect(periodDao.getByGridId(revisedPeriod)).andReturn(period);
        amendmentService.updateDevelopmentAmendment(period, Add.create(expectedPlannedActivity));
        expect(studyService.saveStudyFor(period)).andReturn(study);
    }

    private void expectSuccessfulDrillDown() {
        revisedStudy = revise(study, devAmendment);
        revisedPeriod = revisedStudy.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(1).getPeriods().first();
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

    @SuppressWarnings({ "deprecation" }) // URLEncoder.encode is deprecated for stupid reasons
    private void expectEntityFormAttribute(String name, String value) throws IOException {
        StringBuilder form = new StringBuilder();
        if (request.getEntity() != null) {
            form.append(request.getEntity().getText()).append('&');
        }
        form.append(URLEncoder.encode(name)).
            append('=').append(URLEncoder.encode(value));
        request.setEntity(form.toString(), MediaType.APPLICATION_WWW_FORM);
    }

    private void expectMinimumPostAttributes() throws IOException {
        expectEntityFormAttribute("day", DAY.toString());
        expectEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectEntityFormAttribute("activity-code", ACTIVITY_CODE);
    }
}
