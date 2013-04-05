/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivityFormTest extends RestletTestCase {
    private static final Integer DAY = 7;
    private static final Integer WEIGHT = 8;
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
        expectRequestEntityFormAttribute("weight", WEIGHT.toString());

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Missing required parameter day", form.getErrors().get(0));
    }


    public void testDayMustBeInteger() throws Exception {
        expectRequestEntityFormAttribute("day", "twelve");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);
        expectRequestEntityFormAttribute("weight", WEIGHT.toString());

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Parameter day must be an integer ('twelve' isn't)", form.getErrors().get(0));
    }

    public void testWeightMustBeInteger() throws Exception {
        expectRequestEntityFormAttribute("day", "12");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);
        expectRequestEntityFormAttribute("weight", "eight");

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Parameter weight must be an integer ('eight' isn't)", form.getErrors().get(0));
    }

    public void testRequiresActivitySource() throws Exception {
        expectRequestEntityFormAttribute("day", "7");
        expectRequestEntityFormAttribute("weight", "10");
        expectRequestEntityFormAttribute("activity-code", ACTIVITY_CODE);

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Missing required parameter activity-source", form.getErrors().get(0));
    }

    public void testRequiresActivityCode() throws Exception {
        expectRequestEntityFormAttribute("day", "7");
        expectRequestEntityFormAttribute("weight", "10");
        expectRequestEntityFormAttribute("activity-source", ACTIVITY_SOURCE_NAME);

        PlannedActivityForm form = createForm();

        assertEquals("Wrong number of errors", 1, form.getErrors().size());
        assertEquals("Missing required parameter activity-code", form.getErrors().get(0));
    }

    public void testCreateDescribedPlannedActivityThrowsExceptionWhenInvalid() throws Exception {
        expectRequestEntityFormAttribute("day", "7");
        expectRequestEntityFormAttribute("weight", "10");
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
        assertEquals((Integer)7, actual.getDay());
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

    public void testWithLabelForAllReps() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "research");

        PlannedActivity actual = createPlannedActivityFromForm();
        assertNotNull(actual);

        assertEquals("Wrong number of labels", 1, actual.getPlannedActivityLabels().size());
        assertPlannedActivityLabel("Single label", "research", null, actual.getPlannedActivityLabels().first());
    }

    public void testBlankLabelIgnored() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "");

        PlannedActivity actual = createPlannedActivityFromForm();
        assertNotNull(actual);

        assertEquals("Wrong number of labels", 0, actual.getPlannedActivityLabels().size());
    }

    public void testWithLabelForAllRepsWithTrailingSemicolon() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "research;");

        PlannedActivity actual = createPlannedActivityFromForm();
        assertNotNull(actual);

        assertEquals("Wrong number of labels", 1, actual.getPlannedActivityLabels().size());
        assertPlannedActivityLabel("Single label", "research", null, actual.getPlannedActivityLabels().first());
    }

    public void testWithLabelForSomeReps() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "soc;1 3\t5");

        PlannedActivity actual = createPlannedActivityFromForm();
        assertNotNull(actual);

        assertEquals("Wrong number of labels", 3, actual.getPlannedActivityLabels().size());
        List<PlannedActivityLabel> actualLabels = new ArrayList<PlannedActivityLabel>(actual.getPlannedActivityLabels());
        assertPlannedActivityLabel("PAL 0", "soc", 1, actualLabels.get(0));
        assertPlannedActivityLabel("PAL 1", "soc", 3, actualLabels.get(1));
        assertPlannedActivityLabel("PAL 2", "soc", 5, actualLabels.get(2));
    }

    public void testWithMultipleLabels() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "soc;1 3 5");
        expectRequestEntityFormAttribute("label", "important; 5 2");
        expectRequestEntityFormAttribute("label", "shiny");

        PlannedActivity actual = createPlannedActivityFromForm();
        assertNotNull(actual);

        assertEquals("Wrong number of labels", 6, actual.getPlannedActivityLabels().size());
        List<PlannedActivityLabel> actualLabels = new ArrayList<PlannedActivityLabel>(actual.getPlannedActivityLabels());
        assertPlannedActivityLabel("PAL 0", "important", 2, actualLabels.get(0));
        assertPlannedActivityLabel("PAL 1", "important", 5, actualLabels.get(1));
        assertPlannedActivityLabel("PAL 2", "shiny", null, actualLabels.get(2));
        assertPlannedActivityLabel("PAL 3", "soc", 1, actualLabels.get(3));
        assertPlannedActivityLabel("PAL 4", "soc", 3, actualLabels.get(4));
        assertPlannedActivityLabel("PAL 5", "soc", 5, actualLabels.get(5));
    }

    public void testWithInvalidNumberInLabelSpec() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "soc;1 B");

        try {
            createPlannedActivityFromForm();
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("The label 'soc;1 B' is invalid.  All rep numbers must be nonnegative integers.",
                scve.getMessage());
        }
    }

    public void testWithNegativeNumberInLabelSpec() throws Exception {
        expectMinimumEntityAttributes();
        expectFindActivity();

        expectRequestEntityFormAttribute("label", "soc;4 -7");

        try {
            createPlannedActivityFromForm();
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("The label 'soc;4 -7' is invalid.  All rep numbers must be nonnegative integers.",
                scve.getMessage());
        }
    }

    private void assertPlannedActivityLabel(String message, String expectedLabel, Integer expectedRepNumber, PlannedActivityLabel actual) {
        assertEquals(message + ": wrong label", expectedLabel, actual.getLabel());
        if (expectedRepNumber == null) {
            assertNull(message + ": should be for all reps", actual.getRepetitionNumber());
        } else {
            assertEquals(message + ": wrong rep number", expectedRepNumber, actual.getRepetitionNumber());
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
        expectRequestEntityFormAttribute("weight", WEIGHT.toString());        
    }
}
