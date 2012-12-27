/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityPopulationMutatorTest extends StudyCalendarTestCase {
    private ChangePlannedActivityPopulationMutator mutator;

    private Study study;
    private PlannedActivity plannedActivity;
    private ScheduledCalendar scheduledCalendar;

    private ScheduledActivityDao scheduledActivityDao;
    private PopulationDao populationDao;
    private Population p1, p2;
    private PropertyChange change;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivity = createPlannedActivity("Elph", 4);
        plannedActivity.setPopulation(p1);
        Period period = createPeriod(1, 7, 3);
        period.addPlannedActivity(plannedActivity);

        study = createBasicTemplate();
        study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).addPeriod(period);
        scheduledCalendar = new ScheduledCalendar();

        p1 = createPopulation("P1", "people");
        p2 = createPopulation("P2", "persons");

        // For side effects
        change = PropertyChange.create("population", "P1", "P2");
        Delta.createDeltaFor(plannedActivity, change);


        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        populationDao = registerDaoMockFor(PopulationDao.class);
    }

    private Mutator getMutator() {
        if (mutator == null) {
            mutator = new ChangePlannedActivityPopulationMutator(
                change, study, scheduledActivityDao, populationDao);
        }
        return mutator;
    }

    public void testApplyPopulationToPlannedActivity() throws Exception {
        expectFindPopulationP2();

        replayMocks();
        getMutator().apply(plannedActivity);
        verifyMocks();
        assertEquals("Wrong population", p2, plannedActivity.getPopulation());
    }

    public void testApplyNullPopulationToPlannedActivity() throws Exception {
        change.setNewValue(null);

        replayMocks();
        getMutator().apply(plannedActivity);
        verifyMocks();
        assertNull("Wrong population", plannedActivity.getPopulation());
    }

    private void expectFindPopulationP2() {
        expect(populationDao.getByAbbreviation(study, "P2")).andReturn(p2);
    }
}