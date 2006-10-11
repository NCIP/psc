package edu.northwestern.bioinformatics.studycalendar.service;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ParticipantServiceTest extends StudyCalendarTestCase {
    private ParticipantDao participantDao;
    private ParticipantService service;

    protected void setUp() throws Exception {
        super.setUp();
        participantDao = registerMockFor(ParticipantDao.class);

        service = new ParticipantService();
        service.setParticipantDao(participantDao);
    }

    public void testAssignParticipant() throws Exception {
        Study study = createNamedInstance("Glancing", Study.class);
        Site site = createNamedInstance("Lake", Site.class);
        StudySite studySite = createStudySite(study, site);
        Participant participantIn = createParticipant("Alice", "Childress");
        Date startDate = DateUtils.createDate(2006, Calendar.OCTOBER, 31);
        Arm expectedArm = createEpoch("Treatment", "A", "B", "C").getArms().get(1);
        expectedArm.addPeriod(createPeriod("DC", 1, 7, 1));
        expectedArm.getPeriods().iterator().next().addPlannedEvent(createPlannedEvent("Any", 4));

        Participant participantExpectedSave = createParticipant("Alice", "Childress");

        StudyParticipantAssignment expectedAssignment = new StudyParticipantAssignment();
        expectedAssignment.setStartDateEpoch(startDate);
        expectedAssignment.setParticipant(participantExpectedSave);
        expectedAssignment.setStudySite(studySite);

        participantExpectedSave.addAssignment(expectedAssignment);

        participantDao.save(participantExpectedSave);
        expectLastCall().times(2);
        replayMocks();

        StudyParticipantAssignment actualAssignment = service.assignParticipant(participantIn, studySite, expectedArm, startDate);
        verifyMocks();

        assertNotNull("Assignment not returned", actualAssignment);
        assertEquals("Assignment not added to participant", 1, participantIn.getAssignments().size());
        assertEquals("Assignment not added to participant", actualAssignment, participantIn.getAssignments().get(0));

        assertNotNull(actualAssignment.getScheduledCalendar());
        assertEquals(1, actualAssignment.getScheduledCalendar().getScheduledArms().size());
        ScheduledArm scheduledArm = actualAssignment.getScheduledCalendar().getScheduledArms().get(0);
        assertEquals(expectedArm, scheduledArm.getArm());
        assertPositive("No scheduled events", scheduledArm.getEvents().size());
    }

    public void testScheduleFirstArm() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        assignment.setParticipant(createParticipant("Alice", "Childress"));
        participantDao.save(assignment.getParticipant());
        replayMocks();

        Arm arm = createNamedInstance("A", Arm.class);
        Period p1 = createPeriod("P1", 1, 7, 3);
        Period p2 = createPeriod("P2", 3, 1, 1);
        Period p3 = createPeriod("P3", 8, 28, 2);
        arm.addPeriod(p1);
        arm.addPeriod(p2);
        arm.addPeriod(p3);
                                                                              // days
        p1.addPlannedEvent(setId(1, createPlannedEvent("CBC", 1)));           // 1, 8, 15
        p1.addPlannedEvent(setId(2, createPlannedEvent("Vitals", 3)));        // 3, 10, 17
        p2.addPlannedEvent(setId(3, createPlannedEvent("Questionnaire", 1))); // 3
        p3.addPlannedEvent(setId(4, createPlannedEvent("Infusion", 1)));      // 8, 36
        p3.addPlannedEvent(setId(5, createPlannedEvent("Infusion", 18)));     // 25, 53

        service.scheduleArm(assignment, arm, DateUtils.createDate(2006, Calendar.APRIL, 1));
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Arm not added to scheduled arms", 1, scheduledCalendar.getScheduledArms().size());
        assertSame("Arm not added to scheduled arms", arm, scheduledCalendar.getScheduledArms().get(0).getArm());
        List<ScheduledEvent> events = scheduledCalendar.getScheduledArms().get(0).getEvents();
        assertEquals("Wrong number of events added", 11, events.size());

        assertNewlyScheduledEvent(2006, Calendar.APRIL,  1, 1, events.get(0));
        assertNewlyScheduledEvent(2006, Calendar.APRIL,  3, 2, events.get(1));
        assertNewlyScheduledEvent(2006, Calendar.APRIL,  3, 3, events.get(2));
        assertNewlyScheduledEvent(2006, Calendar.APRIL,  8, 1, events.get(3));
        assertNewlyScheduledEvent(2006, Calendar.APRIL,  8, 4, events.get(4));
        assertNewlyScheduledEvent(2006, Calendar.APRIL, 10, 2, events.get(5));
        assertNewlyScheduledEvent(2006, Calendar.APRIL, 15, 1, events.get(6));
        assertNewlyScheduledEvent(2006, Calendar.APRIL, 17, 2, events.get(7));
        assertNewlyScheduledEvent(2006, Calendar.APRIL, 25, 5, events.get(8));
        assertNewlyScheduledEvent(2006, Calendar.MAY,    6, 4, events.get(9));
        assertNewlyScheduledEvent(2006, Calendar.MAY,   23, 5, events.get(10));
    }

    private void assertNewlyScheduledEvent(
        int expectedYear, int expectedMonth, int expectedDayOfMonth,
        int expectedPlannedEventId, ScheduledEvent actualEvent
    ) {
        assertEquals("Wrong associated planned event", expectedPlannedEventId, (int) actualEvent.getPlannedEvent().getId());
        assertDayOfDate("Wrong ideal date", expectedYear, expectedMonth, expectedDayOfMonth, actualEvent.getIdealDate());
        assertTrue("Wrong current state mode", actualEvent.getCurrentState() instanceof Scheduled);
        Scheduled currentState = (Scheduled) actualEvent.getCurrentState();
        assertEquals("Current and ideal date not same", actualEvent.getIdealDate(), currentState.getDate());
        assertEquals("Wrong reason", "Initialized from template", currentState.getReason());
    }
}
