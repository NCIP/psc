/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public abstract class PeriodMutatorTestCase<C extends Change> extends StudyCalendarTestCase {
    protected static final int PERIOD_0_REPS = 3;
    protected static final int PERIOD_1_REPS = 2;
    protected static final Date STUDY_SEGMENT_START_DATE = DateTools.createDate(2010, Calendar.MARCH, 1);
    protected static final String REVISION_DISPLAY_NAME = "02/04/1909 (Oops)";

    protected Amendment amendment;
    private Delta<?> delta;
    protected C change;

    protected StudySegment studySegment;
    /** period0 is the period to which the delta applies */
    protected Period period0;
    /** period1 is the period to which the delta applies */
    protected Period period1;
    protected PlannedActivity p0e0, p0e1, p1e0, p1e1;

    protected ScheduledCalendar scheduledCalendar;
    protected ScheduledStudySegment scheduledStudySegment;
    /** Indexes are [period][event][repetition] */
    private ScheduledActivity[][][] scheduledActivities;

    private Mutator mutator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySegment = Epoch.create("E1", "A", "B").getStudySegments().get(0);
        period0 = Fixtures.createPeriod("P0", 1, 9, PERIOD_0_REPS);
        period0.addPlannedActivity(p0e0 = Fixtures.createPlannedActivity("P0.E0", 2));
        period0.addPlannedActivity(p0e1 = Fixtures.createPlannedActivity("P0.E1", 6));
        period1 = Fixtures.createPeriod("P1", 1, 11, PERIOD_1_REPS);
        period1.addPlannedActivity(p1e0 = Fixtures.createPlannedActivity("P1.E0", 1));
        period1.addPlannedActivity(p1e1 = Fixtures.createPlannedActivity("P1.E1", 4));

        change = createChange();
        delta = createDelta();
        amendment = Fixtures.createAmendments("Oops");
        amendment.setDate(DateTools.createDate(1909, Calendar.FEBRUARY, 4));
        amendment.addDelta(delta);
        scheduledCalendar = new ScheduledCalendar();
        scheduledStudySegment = Fixtures.createScheduledStudySegment(studySegment);
        scheduledStudySegment.setStartDay(1);
        scheduledStudySegment.setStartDate(STUDY_SEGMENT_START_DATE);
        scheduledCalendar.addStudySegment(scheduledStudySegment);
        scheduledCalendar.setAssignment(new StudySubjectAssignment());

        SubjectService subjectService = new SubjectService() {
            @Override
            protected ScheduledActivity createEmptyScheduledActivityFor(PlannedActivity event) {
                return createUnschedulableMockEvent(event);
            }
        };
        subjectService.schedulePeriod(period0, amendment, "DC", scheduledStudySegment);
        subjectService.schedulePeriod(period1, amendment, "DC", scheduledStudySegment);
        scheduledActivities = new ScheduledActivity[2][2][3];
        for (ScheduledActivity event : scheduledStudySegment.getActivities()) {
            int period, pe;
            if      (event.getPlannedActivity() == p0e0) { period = 0; pe = 0; }
            else if (event.getPlannedActivity() == p0e1) { period = 0; pe = 1; }
            else if (event.getPlannedActivity() == p1e0) { period = 1; pe = 0; }
            else if (event.getPlannedActivity() == p1e1) { period = 1; pe = 1; }
            else throw new Error("Test setup failure: not all planned events accounted for");

            scheduledActivities[period][pe][event.getRepetitionNumber()] = event;
        }
    }

    protected final ScheduledActivity getScheduledActivityFixture(PlannedActivity plannedActivity, int repetition) {
        if      (plannedActivity == p0e0) { return getScheduledActivityFixture(0, 0, repetition); }
        else if (plannedActivity == p0e1) { return getScheduledActivityFixture(0, 1, repetition); }
        else if (plannedActivity == p1e0) { return getScheduledActivityFixture(1, 0, repetition); }
        else if (plannedActivity == p1e1) { return getScheduledActivityFixture(1, 1, repetition); }
        else throw new Error("Test setup failure: non-fixture PE: " + plannedActivity);
    }

    protected final ScheduledActivity getScheduledActivityFixture(int period, int plannedActivity, int repetition) {
        return scheduledActivities[period][plannedActivity][repetition];
    }

    protected abstract C createChange();

    protected Delta<?> createDelta() {
        return Delta.createDeltaFor(period0, change);
    }

    protected abstract Mutator createMutator();

    protected Mutator getMutator() {
        if (mutator == null) mutator = createMutator();
        return mutator;
    }

    public void testApplicableToLiveSchedules() throws Exception {
        assertTrue(getMutator().appliesToExistingSchedules());
    }

    private ScheduledActivity createUnschedulableMockEvent(PlannedActivity event) {
        ScheduledActivity semimock;
        try {
            semimock = registerMockFor(ScheduledActivity.class,
            ScheduledActivity.class.getMethod("unscheduleIfOutstanding", String.class));
        } catch (NoSuchMethodException e) {
            throw new Error("This shouldn't happen", e);
        }
        semimock.setPlannedActivity(event);
        return semimock;
    }
}
