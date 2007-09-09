package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;

/**
 * Note that some tests here are more like integration tests in that they test the full
 * DeltaService/MutatorFactory/Mutator stack.
 *
 * @author Rhett Sutphin
 */
public class DeltaServiceTest extends StudyCalendarTestCase {
    private Study study;
    private PlannedCalendar calendar;
    private DeltaService service;

    private AmendmentDao amendmentDao;
    private DeltaDao deltaDao;
    private EpochDao epochDao;
    private ArmDao armDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        study = setGridId("STUDY-GRID", setId(300, TemplateSkeletonCreator.BASIC.create()));
        calendar = setGridId("CAL-GRID", setId(400, study.getPlannedCalendar()));
        Epoch e1 = setGridId("E1-GRID", setId(1, calendar.getEpochs().get(1)));
        Epoch e2 = setGridId("E2-GRID", setId(2, calendar.getEpochs().get(2)));
        Arm e1a0 = setGridId("E1A0-GRID",
            setId(10, calendar.getEpochs().get(1).getArms().get(0)));

        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        study.setAmendment(a3);

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
        Study actual = service.getAmendedStudy(study, study.getAmendment());
        assertEquals(study.getAmendment(), actual.getAmendment());
        assertTrue("Amended study is not marked transient", actual.isMemoryOnly());
    }

    public void testGetAmendedWhenAmendmentNotRelevant() throws Exception {
        study.setName("Study E");
        Amendment irrelevant = setGridId("B0-GRID", createAmendments("B0"));
        try {
            service.getAmendedStudy(study, irrelevant);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Amendment B0 (B0-GRID) does not apply to the template for Study E (STUDY-GRID)", e.getMessage());
        }
    }

    public void testGetAmendedOneRevBack() throws Exception {
        assertEquals("Test setup failure", 3, calendar.getEpochs().size());

        replayMocks();
        Study amended
            = service.getAmendedStudy(study, study.getAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);
        assertEquals("Amended calendar reflects incorrect level", "A2",
            amended.getAmendment().getName());

        List<Epoch> actualEpochs = amended.getPlannedCalendar().getEpochs();
        List<Arm> actualE1Arms = actualEpochs.get(1).getArms();
        assertEquals("Arm add not reverted: " + actualE1Arms, 2, actualE1Arms.size());
        assertEquals("Epoch add incorrectly reverted: " + actualEpochs, 3, actualEpochs.size());
    }

    public void testGetAmendedTwoRevsBack() throws Exception {
        assertEquals("Test setup failure", 3, calendar.getEpochs().size());
        assertEquals("Test setup failure", 3, calendar.getEpochs().get(1).getArms().size());

        replayMocks();
        Study amended
            = service.getAmendedStudy(study, study.getAmendment().getPreviousAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);

        List<Arm> actualE1Arms = amended.getPlannedCalendar().getEpochs().get(1).getArms();
        assertEquals("Arm add in A3 not reverted: " + actualE1Arms, 2, actualE1Arms.size());
        assertEquals("Epoch add in A2 not reverted: " + amended.getPlannedCalendar().getEpochs(), 2,
            amended.getPlannedCalendar().getEpochs().size());
        assertEquals("Amended calendar reflects incorrect level", "A1",
            amended.getAmendment().getName());
    }

    public void testRevise() throws Exception {
        assertEquals("Wrong number of epochs to start with", 3, calendar.getEpochs().size());

        Amendment inProgress = new Amendment();
        Epoch newEpoch = setGridId("E-NEW", setId(8, Epoch.create("Long term")));
        inProgress.addDelta(Delta.createDeltaFor(calendar, createAddChange(8, null)));

        expect(epochDao.getById(8)).andReturn(newEpoch).anyTimes();

        replayMocks();
        Study revised = service.revise(study, inProgress);
        verifyMocks();

        assertEquals("Epoch not added", 4, revised.getPlannedCalendar().getEpochs().size());
        assertEquals("Epoch not added in the expected location", 8,
            (int) revised.getPlannedCalendar().getEpochs().get(3).getId());

        assertEquals("Original calendar modified", 3, calendar.getEpochs().size());
    }

    /* TODO:
    public void testSaveRevision() throws Exception {
        PlannedCalendarDelta delta = new PlannedCalendarDelta(calendar);
        Epoch added = new Epoch();
        Add add = new Add();
        add.setNewChild(added);
        delta.addChange(add);

        Amendment revision = new Amendment("Rev to save");
        revision.addDelta(delta);

        epochDao.save(added);
        deltaDao.save(delta);
        amendmentDao.save(revision);
        replayMocks();
        service.saveRevision(revision);
        verifyMocks();
    }
    */
}
