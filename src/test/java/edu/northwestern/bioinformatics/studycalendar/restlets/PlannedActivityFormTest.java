package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivityFormTest extends RestletTestCase {
    private static final Integer DAY = 7;
    private static final String ACTIVITY_CODE = "F";
    private static final Activity ACTIVITY
        = Fixtures.createActivity("Fool", ACTIVITY_CODE,
            Fixtures.DEFAULT_ACTIVITY_SOURCE, Fixtures.DEFAULT_ACTIVITY_TYPE);
    private static final String ACTIVITY_SOURCE_NAME
        = Fixtures.DEFAULT_ACTIVITY_SOURCE.getName();
    private static final Population POPULATION = Fixtures.createPopulation("T", "Tea");

    private Study study;
    private ActivityDao activityDao;
    private PopulationDao populationDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = Fixtures.createBasicTemplate();
        activityDao = registerDaoMockFor(ActivityDao.class);
        populationDao = registerDaoMockFor(PopulationDao.class);
    }

    private PlannedActivityForm createForm() {
        return new PlannedActivityForm(request.getEntity(), study, activityDao, populationDao);
    }

    private PlannedActivity createPlannedActivityFromForm() throws ResourceException {
        replayMocks();
        PlannedActivityForm form = createForm();
        PlannedActivity actual = form.createDescribedPlannedActivity();
        verifyMocks();
        return actual;
    }

    public void testRequiresDay() throws Exception {
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Missing required parameter day", form.getErrors().get(0));
    }

    public void testDayMustBeInteger() throws Exception {
        expectRequestEntityFormAttribute("day", "twelve");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Parameter day must be an integer ('twelve' isn't)", form.getErrors().get(0));
    }

    public void testRequiresActivitySource() throws Exception {
        expectRequestEntityFormAttribute("day", "7");
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Missing required parameter activity-source", form.getErrors().get(0));
    }

    public void testRequiresActivityCode() throws Exception {
        expectRequestEntityFormAttribute("day", "7");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Missing required parameter activity-code", form.getErrors().get(0));
    }

    public void testCreateDescribedPlannedActivityThrowsExceptionWhenInvalid() throws Exception {
        expectRequestEntityFormAttribute("day", "7");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);

        PlannedActivityForm form = createForm();
        try {
            form.createDescribedPlannedActivity();
            fail("Exception not thrown");
        } catch (ResourceException actual) {
            assertEquals("Wrong status", 422, actual.getStatus().getCode());
            assertEquals(form.getErrors().get(0), actual.getStatus().getDescription());
        }
    }

    public void testCreateMinimalPlannedActivity() throws Exception {
        expectMinimumEntityAttributes();

        expectFindActivity();

        PlannedActivity actual = createPlannedActivityFromForm();

        assertNotNull(actual);
        assertEquals((Object) 7, actual.getDay());
        assertEquals(ACTIVITY, actual.getActivity());
    }

    public void testWithDetailsAndCondition() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();
        expectRequestEntityFormAttribute("details", "foom");
        expectRequestEntityFormAttribute("condition", "foom > 12");

        PlannedActivity actual = createPlannedActivityFromForm();

        assertNotNull(actual);
        assertEquals("foom", actual.getDetails());
        assertEquals("foom > 12", actual.getCondition());
    }

    public void testAddPlannedActivityWhenActivityDoesNotExist() throws Exception {
        expectMinimumEntityAttributes();
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_CODE, ACTIVITY_SOURCE_NAME)).
            andReturn(null);

        try {
            createPlannedActivityFromForm();
            fail("Exception not thrown");
        } catch (ResourceException actual) {
            assertEquals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, actual.getStatus());
            assertEquals("Activity not found", actual.getStatus().getDescription());
        }
    }

    public void testWithPopulation() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();
        expectRequestEntityFormAttribute("population", "T");
        expect(populationDao.getByAbbreviation(study, "T")).andReturn(POPULATION);

        PlannedActivity actual = createPlannedActivityFromForm();

        assertNotNull(actual);
        assertEquals(POPULATION, actual.getPopulation());
    }

    public void testWithPopulationWhenPopulationDoesNotExist() throws Exception {
        expectMinimumEntityAttributes();
        expectRequestEntityFormAttribute("population", "T");
        expectFindActivity();
        expect(populationDao.getByAbbreviation(study, "T")).andReturn(null);

        try {
            createPlannedActivityFromForm();
            fail("Exception not thrown");
        } catch (ResourceException actual) {
            assertEquals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, actual.getStatus());
            assertEquals("Population not found", actual.getStatus().getDescription());
        }
    }

    ////// EXPECTATIONS

    private void expectFindActivity() {
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_CODE, ACTIVITY_SOURCE_NAME)).
            andReturn(ACTIVITY);
    }

    private void expectMinimumEntityAttributes() throws IOException {
        expectRequestEntityFormAttribute("day", DAY.toString());
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);
    }
}
