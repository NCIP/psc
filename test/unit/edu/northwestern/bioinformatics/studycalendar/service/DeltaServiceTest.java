package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;

/**
 * Note that some tests here are more like integration tests in that they test the full
 * DeltaService/MutatorFactory/Mutator stack.
 *
 * @author Rhett Sutphin
 */
public class DeltaServiceTest extends StudyCalendarTestCase {
    private PlannedCalendar calendar;
    private DeltaService service;

    private EpochDao epochDao;
    private ArmDao armDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calendar = setGridId("CAL-GRID",
            setId(400, TemplateSkeletonCreator.BASIC.create().getPlannedCalendar()));
        Epoch e1 = setGridId("E1-GRID", setId(1, calendar.getEpochs().get(1)));
        Epoch e2 = setGridId("E2-GRID", setId(2, calendar.getEpochs().get(2)));
        Arm e1a0 = setGridId("E1A0-GRID",
            setId(10, calendar.getEpochs().get(1).getArms().get(0)));

        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        Amendment a1 = a2.getPreviousAmendment();
        Amendment a0 = a1.getPreviousAmendment();
        calendar.setAmendment(a3);

        a2.addDelta(Delta.createDeltaFor(calendar, createAddChange(2, null)));
        a3.addDelta(Delta.createDeltaFor(e1, createAddChange(10, 0)));

        epochDao = registerDaoMockFor(EpochDao.class);
        armDao = registerDaoMockFor(ArmDao.class);
        expect(epochDao.getById(2)).andReturn(e2).anyTimes();
        expect(armDao.getById(10)).andReturn(e1a0).anyTimes();

        MutatorFactory mutatorFactory = new MutatorFactory();
        mutatorFactory.setDaoFinder(new StaticDaoFinder(epochDao, armDao));

        service = new DeltaService();
        service.setMutatorFactory(mutatorFactory);
    }

    public void testGetAmendedWhenAtAmendedLevel() throws Exception {
        PlannedCalendar actual = service.getAmendedCalendar(calendar, calendar.getAmendment());
        assertEquals(calendar.getAmendment(), actual.getAmendment());
        assertTrue("Amended calendar is not marked transient", actual.isMemoryOnly());
    }

    public void testGetAmendedWhenAmendmentNotRelevant() throws Exception {
        calendar.getStudy().setName("Study E");
        calendar.setGridId("CAL-GRID");
        Amendment irrelevant = setGridId("B0-GRID", createAmendments("B0"));
        try {
            service.getAmendedCalendar(calendar, irrelevant);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Amendment B0 (B0-GRID) does not apply to the template for Study E (CAL-GRID)", e.getMessage());
        }
    }

    public void testGetAmendedOneRevBack() throws Exception {
        assertEquals("Test setup failure", 3, calendar.getEpochs().size());

        replayMocks();
        PlannedCalendar amended
            = service.getAmendedCalendar(calendar, calendar.getAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);
        assertEquals("Amended calendar reflects incorrect level", "A2",
            amended.getAmendment().getName());

        List<Arm> actualE1Arms = amended.getEpochs().get(1).getArms();
        assertEquals("Arm add not reverted: " + actualE1Arms, 2, actualE1Arms.size());
        assertEquals("Epoch add incorrectly reverted: " + amended.getEpochs(), 3, amended.getEpochs().size());
    }

    public void testGetAmendedTwoRevsBack() throws Exception {
        assertEquals("Test setup failure", 3, calendar.getEpochs().size());
        assertEquals("Test setup failure", 3, calendar.getEpochs().get(1).getArms().size());

        replayMocks();
        PlannedCalendar amended
            = service.getAmendedCalendar(calendar, calendar.getAmendment().getPreviousAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);

        List<Arm> actualE1Arms = amended.getEpochs().get(1).getArms();
        assertEquals("Arm add in A3 not reverted: " + actualE1Arms, 2, actualE1Arms.size());
        assertEquals("Epoch add in A2 not reverted: " + amended.getEpochs(), 2,
            amended.getEpochs().size());
        assertEquals("Amended calendar reflects incorrect level", "A1",
            amended.getAmendment().getName());
    }
}
