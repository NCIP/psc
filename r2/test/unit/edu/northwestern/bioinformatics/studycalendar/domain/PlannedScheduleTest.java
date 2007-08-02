package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class PlannedScheduleTest extends StudyCalendarTestCase {
    private PlannedSchedule schedule;

    protected void setUp() throws Exception {
        super.setUp();
        schedule = new PlannedSchedule();
    }

    public void testAddEpoch() throws Exception {
        Epoch epoch = new Epoch();
        schedule.addEpoch(epoch);
        assertEquals("Wrong number of epochs", 1, schedule.getEpochs().size());
        assertSame("Wrong epoch present", epoch, schedule.getEpochs().get(0));
        assertSame("Bidirectional relationship not maintained", schedule, epoch.getPlannedSchedule());
    }

    public void testSetStudy() throws Exception {
        Study study = new Study();
        schedule.setStudy(study);
        assertSame("Bidirectional relationship not maintained", study.getPlannedSchedule(), schedule);
    }
    
    public void testSetStudyWhenAlreadySet() throws Exception {
        Study study = new Study();
        study.setPlannedSchedule(schedule);
        schedule.setStudy(study); // we are really checking for an infinite loop on this call
        assertSame("Bidirectional relationship not maintained", study.getPlannedSchedule(), schedule);
    }

    public void testLength() throws Exception {
        Epoch e1 = registerMockFor(Epoch.class);
        expect(e1.getLengthInDays()).andReturn(45);
        e1.setPlannedSchedule(schedule);

        Epoch e2 = registerMockFor(Epoch.class);
        expect(e2.getLengthInDays()).andReturn(13);
        e2.setPlannedSchedule(schedule);

        replayMocks();

        schedule.addEpoch(e1);
        schedule.addEpoch(e2);
        assertEquals(45, schedule.getLengthInDays());
        verifyMocks();
    }
}
