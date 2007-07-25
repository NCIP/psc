package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class PlannedCalendarTest extends StudyCalendarTestCase {
    private PlannedCalendar calendar;

    protected void setUp() throws Exception {
        super.setUp();
        calendar = new PlannedCalendar();
    }

    public void testAddEpoch() throws Exception {
        Epoch epoch = new Epoch();
        calendar.addEpoch(epoch);
        assertEquals("Wrong number of epochs", 1, calendar.getEpochs().size());
        assertSame("Wrong epoch present", epoch, calendar.getEpochs().get(0));
        assertSame("Bidirectional relationship not maintained", calendar, epoch.getPlannedCalendar());
    }

    public void testSetStudy() throws Exception {
        Study study = new Study();
        calendar.setStudy(study);
        assertSame("Bidirectional relationship not maintained", study.getPlannedCalendar(), calendar);
    }
    
    public void testSetStudyWhenAlreadySet() throws Exception {
        Study study = new Study();
        study.setPlannedCalendar(calendar);
        calendar.setStudy(study); // we are really checking for an infinite loop on this call
        assertSame("Bidirectional relationship not maintained", study.getPlannedCalendar(), calendar);
    }

    public void testLength() throws Exception {
        Epoch e1 = registerMockFor(Epoch.class);
        expect(e1.getLengthInDays()).andReturn(45);
        e1.setParent(calendar);

        Epoch e2 = registerMockFor(Epoch.class);
        expect(e2.getLengthInDays()).andReturn(13);
        e2.setParent(calendar);

        replayMocks();

        calendar.addEpoch(e1);
        calendar.addEpoch(e2);
        assertEquals(45, calendar.getLengthInDays());
        verifyMocks();
    }
    
    public void testGetNameReturnsStudyName() throws Exception {
        Study study = Fixtures.createNamedInstance("Protocol", Study.class);
        study.setPlannedCalendar(calendar);

        assertEquals("Protocol", calendar.getName());
    }

    public void testMaxArmCount() throws Exception {
        calendar.addEpoch(Epoch.create("E1"));
        calendar.addEpoch(Epoch.create("E2", "A", "B", "C"));
        calendar.addEpoch(Epoch.create("E3", "A", "B"));

        assertEquals(3, calendar.getMaxArmCount());
    }
}
