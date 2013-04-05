/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.easymock.classextension.EasyMock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tests for {@link edu.northwestern.bioinformatics.studycalendar.service.SubjectService}'s
 * population-related methods.
 *
 * @author Rhett Sutphin
 */
public class SubjectServicePopulationTest extends StudyCalendarTestCase {
    private SubjectService service;
    private AmendmentService amendmentService;

    private Study study;
    private StudySubjectAssignment ladyPatient;
    private Population females, oldFolks;
    private PlannedActivity forFemales, forOldFolks;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        females = createNamedInstance("F", Population.class);
        females.setAbbreviation("F");
        oldFolks = createNamedInstance("O", Population.class);
        oldFolks.setAbbreviation("OF");

        study = createSingleEpochStudy("S", "Treatment");
        Period period = createPeriod("dc", 1, 14, 1);
        StudySegment studySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        studySegment.addPeriod(period);
        forFemales = createPlannedActivity("Pregnancy test", 4);
        forFemales.setPopulation(females);
        forOldFolks = createPlannedActivity("Senescence test", 2);
        forOldFolks.setPopulation(oldFolks);
        period.addPlannedActivity(forFemales);
        period.addPlannedActivity(forOldFolks);

        ladyPatient = new StudySubjectAssignment();
        ladyPatient.addPopulation(females);
        ladyPatient.setScheduledCalendar(new ScheduledCalendar());
        ScheduledStudySegment schedSegment = createScheduledStudySegment(studySegment);
        ladyPatient.getScheduledCalendar().addStudySegment(schedSegment);
        ladyPatient.setCurrentAmendment(study.getAmendment());

        amendmentService = registerMockFor(AmendmentService.class);
        service = new SubjectService();
        service.setAmendmentService(amendmentService);

        service.schedulePeriod(period, study.getAmendment(), "Setup", schedSegment, 1);

        assertEquals("Test setup failure: should be one scheduled activity", 1, schedSegment.getActivities().size());
        assertEquals("Test setup failure: should be one activity from female pop",
            forFemales, schedSegment.getActivities().get(0).getPlannedActivity());
    }

    public void testUpdatePopulationsRemainTheSame() throws Exception {
        replayMocks();
        service.updatePopulations(ladyPatient, Collections.singleton(females));
        verifyMocks();

        assertEquals("Should have same pops", 1, ladyPatient.getPopulations().size());
        assertSame("Should have same pops", females, ladyPatient.getPopulations().iterator().next());
        assertEquals("Should not have touched activities", 1, scheduledSegment().getActivities().size());
    }

    public void testUpdatePopulationsRemoveOne() throws Exception {
        replayMocks();
        service.updatePopulations(ladyPatient, Collections.<Population>emptySet());
        verifyMocks();

        assertEquals("Should have no pops", 0, ladyPatient.getPopulations().size());
        ScheduledActivity activity = scheduledSegment().getActivities().get(0);
        assertEquals("Test assumption failure", forFemales, activity.getPlannedActivity());
        assertEquals("Activity for removed pop not canceled",
            ScheduledActivityMode.CANCELED, activity.getCurrentState().getMode());
        assertEquals("Wrong reason for cancelation",
            "Subject removed from population F",
            activity.getCurrentState().getReason());
    }

    public void testUpdatePopulationsRemoveOneWhenTwoPresent() throws Exception {
        // checking for concurrent modification exception, so order matters
        ladyPatient.setPopulations(new LinkedHashSet<Population>(Arrays.asList(females, oldFolks)));
        replayMocks();
        service.updatePopulations(ladyPatient, Collections.singleton(oldFolks));
        verifyMocks();

        assertEquals("Should have one pop left", 1, ladyPatient.getPopulations().size());
        assertSame("Wrong pop left", oldFolks, ladyPatient.getPopulations().iterator().next());
        ScheduledActivity activity = scheduledSegment().getActivities().get(0);
        assertEquals("Test assumption failure", forFemales, activity.getPlannedActivity());
        assertEquals("Activity for removed pop not canceled",
            ScheduledActivityMode.CANCELED, activity.getCurrentState().getMode());
        assertEquals("Wrong reason for cancelation",
            "Subject removed from population F",
            activity.getCurrentState().getReason());
    }

    public void testUpdatePopulationsRemoveNotOutstanding() throws Exception {
        ScheduledActivity soleActivity = scheduledSegment().getActivities().get(0);
        soleActivity.changeState(ScheduledActivityMode.OCCURRED.createStateInstance(new Date(), "Just testing"));

        replayMocks();
        service.updatePopulations(ladyPatient, Collections.<Population>emptySet());
        verifyMocks();

        assertEquals("Should have no pops", 0, ladyPatient.getPopulations().size());
        assertEquals("Test assumption failure", forFemales, soleActivity.getPlannedActivity());
        assertEquals("Occurred activity should not have been touched",
            ScheduledActivityMode.OCCURRED, soleActivity.getCurrentState().getMode());
        assertEquals("Occurred activity should not have been touched",
            1, soleActivity.getPreviousStates().size());
    }

    public void testUpdatePopulationsAddNew() throws Exception {
        Set<Population> newPops = new HashSet<Population>(Arrays.asList(females, oldFolks));

        StudySegment templateSegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        EasyMock.expect(amendmentService.getAmendedNode(templateSegment, ladyPatient.getCurrentAmendment()))
            .andReturn(templateSegment);
        replayMocks();
        service.updatePopulations(ladyPatient, newPops);
        verifyMocks();

        assertEquals("Wrong number of pops", 2, ladyPatient.getPopulations().size());
        assertContains("Wrong populations", ladyPatient.getPopulations(), females);
        assertContains("Wrong populations", ladyPatient.getPopulations(), oldFolks);

        assertEquals("New pop activity not added", 2, scheduledSegment().getActivities().size());
        ScheduledActivity newActivity = scheduledSegment().getActivities().get(1);
        assertEquals("New activity is from wrong planned", forOldFolks, newActivity.getPlannedActivity());
        assertEquals("New activity isn't SCHEDULED",
            ScheduledActivityMode.SCHEDULED, newActivity.getCurrentState().getMode());
        assertEquals("New activity has wrong scheduling reason",
            "Subject added to population O", newActivity.getCurrentState().getReason());
    }

    private ScheduledStudySegment scheduledSegment() {
        return ladyPatient.getScheduledCalendar().getScheduledStudySegments().get(0);
    }
}
