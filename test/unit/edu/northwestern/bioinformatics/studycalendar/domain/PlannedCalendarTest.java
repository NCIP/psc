package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class PlannedCalendarTest extends StudyCalendarTestCase {
    private PlannedCalendar calendar;

    @Override
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

    public void testTransientClone() throws Exception {
        PlannedCalendar cal = Fixtures.createBasicTemplate().getPlannedCalendar();
        int id = 4;
        cal.setId(id++);
        for (Epoch epoch : cal.getEpochs()) {
            epoch.setId(id++);
            for (Arm arm : epoch.getArms()) {
                arm.setId(id);
            }
        }
        assertNotNull("Test setup failure", cal.getParent());

        PlannedCalendar clone = cal.transientClone();
        assertNotSame("Clone is not a different object", cal, clone);
        assertTrue("Cal not marked mem-only", clone.isMemoryOnly());
        assertNull("parent not cleared from cloned cal", clone.getParent());
        assertEquals("Wrong number of epochs in clone", cal.getEpochs().size(), clone.getEpochs().size());
        for (int i = 0; i < clone.getEpochs().size(); i++) {
            Epoch cloneEpoch = clone.getEpochs().get(i);
            Epoch calEpoch = cal.getEpochs().get(i);
            assertNotSame("Epoch " + i + " is not a different object", calEpoch, cloneEpoch);
            assertTrue("Epoch " + i + " is not marked mem-only", cloneEpoch.isMemoryOnly());
            assertEquals("Epoch " + i + " has a different name", calEpoch.getName(), cloneEpoch.getName());
            assertSame("Epoch " + i + " does not reference its cloned parent", clone, cloneEpoch.getParent());
            assertEquals("Epoch " + i + " has a different number of arms", calEpoch.getArms().size(), cloneEpoch.getArms().size());
            for (int j = 0; j < cloneEpoch.getArms().size(); j++) {
                Arm cloneArm = cloneEpoch.getArms().get(j);
                Arm calArm = calEpoch.getArms().get(j);
                assertNotSame("Arm " + i + " is not a different object", calArm, cloneArm);
                assertTrue("Arm " + i + " is not marked mem-only", cloneArm.isMemoryOnly());
                assertEquals("Arm " + i + " has a different name", calArm.getName(), cloneArm.getName());
                assertSame("Arm " + i + " does not reference its cloned parent", cloneEpoch, cloneArm.getParent());
            }
        }
    }
}
