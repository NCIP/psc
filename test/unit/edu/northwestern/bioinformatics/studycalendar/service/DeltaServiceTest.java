package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

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

    private EpochDao epochDao;
    private ArmDao armDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        study = setGridId("STUDY-GRID", setId(300, createBasicTemplate()));
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

    /* TODO: This should be corrected.  For now, #saveRevision is tested in StudyServiceIntegratedTest
    public void testSaveRevision() throws Exception {
        PlannedCalendarDelta delta = new PlannedCalendarDelta(calendar);
        Epoch added = new Epoch();
        Add add = new Add();
        add.setChild(added);
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

    public void testUpdateRevisionOnNewlyAdded() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, Epoch.create("New"));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            createAddChange(epoch, 3));
        rev.addDelta(pcDelta);
        Arm arm = createNamedInstance("N", Arm.class);

        assertEquals("Wrong number of arms initially", 1, epoch.getArms().size());

        service.updateRevision(rev, epoch, createAddChange(arm, null));

        assertEquals("Arm not directly applied", 2, epoch.getArms().size());
    }

    public void testUpdateRevisionWhenThereIsAlreadyAnApplicableDelta() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, createNamedInstance("New", Epoch.class));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            createAddChange(epoch, 3));
        rev.addDelta(pcDelta);

        assertEquals("Wrong number of changes in delta initially", 1, pcDelta.getChanges().size());

        service.updateRevision(rev, study.getPlannedCalendar(),
            createRemoveChange(study.getPlannedCalendar().getEpochs().get(2)));

        assertEquals("New change not merged into delta", 2, pcDelta.getChanges().size());
    }
    
    public void testUpdateRevisionWhenThereIsNoApplicableDelta() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, createNamedInstance("New", Epoch.class));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            createAddChange(epoch, 3));
        rev.addDelta(pcDelta);

        assertEquals("Wrong number of deltas initially", 1, rev.getDeltas().size());

        Epoch expectedTarget = study.getPlannedCalendar().getEpochs().get(1);
        Add expectedChange = createAddChange(new Arm(), 2);
        service.updateRevision(rev, expectedTarget, expectedChange);

        assertEquals("Wrong number of deltas", 2, rev.getDeltas().size());
        Delta<?> added = rev.getDeltas().get(1);
        assertSame("Added delta is not for correct node", expectedTarget, added.getNode());
        assertEquals("Wrong number of changes in new delta", 1, added.getChanges().size());
        assertEquals("Wrong change in new delta", expectedChange, added.getChanges().get(0));
    }
}
