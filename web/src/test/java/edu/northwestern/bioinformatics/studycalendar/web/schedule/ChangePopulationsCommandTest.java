/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;

import java.util.Collections;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class ChangePopulationsCommandTest extends StudyCalendarTestCase {
    private ChangePopulationsCommand command;
    private StudySubjectAssignment assignment;

    private SubjectService subjectService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectService = registerMockFor(SubjectService.class);
        assignment = new StudySubjectAssignment();
    }

    private void initCommand() {
        command = new ChangePopulationsCommand(assignment, subjectService);
    }

    public void testPopulationsPrepopulatedFromAssignment() throws Exception {
        Population expectedPopulation = Fixtures.setId(4, new Population());
        assignment.setPopulations(Collections.singleton(expectedPopulation));
        initCommand();

        assertEquals("Wrong number of initial populations", 1, command.getPopulations().size());
        assertSame("Wrong initial population", 4, command.getPopulations().iterator().next().getId());
        assertNotSame("Population collection not cloned", assignment.getPopulations(), command.getPopulations());
    }

    public void testApplyInvokesService() throws Exception {
        Set<Population> expectedPopulations = Collections.singleton(new Population());
        initCommand();
        command.setPopulations(expectedPopulations);

        subjectService.updatePopulations(assignment, expectedPopulations);
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
