/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.classextension.EasyMock.expect;

import java.sql.Timestamp;

/**
 * Note that some tests here are more like integration tests in that they test the full
 * DeltaService/MutatorFactory/Mutator stack.
 *
 * @author Rhett Sutphin
 */
public class DeltaServiceTest extends StudyCalendarTestCase {
    private Study study;
    private PlannedCalendar calendar;
    private Epoch e0;
    private Epoch e1;
    private StudySegment e0a0;

    private DeltaService service;
    private TemplateService mockTemplateService;

    private DeltaDao deltaDao;
    private ChangeDao changeDao;
    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;
    private PlannedActivityLabelDao plannedActivityLabelDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        study = setGridId("STUDY-GRID", setId(300, createBasicTemplate()));
        calendar = setGridId("CAL-GRID", setId(400, study.getPlannedCalendar()));
        e0 = setGridId("E0-GRID", setId(1, calendar.getEpochs().get(0)));
        e1 = setGridId("E1-GRID", setId(2, calendar.getEpochs().get(1)));
        e0a0 = setGridId("E0A0-GRID",
            setId(10, calendar.getEpochs().get(0).getStudySegments().get(0)));

        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        study.setAmendment(a3);

        a2.addDelta(Delta.createDeltaFor(calendar, createAddChange(2, null)));
        a3.addDelta(Delta.createDeltaFor(e0, createAddChange(10, 0)));

        changeDao = registerDaoMockFor(ChangeDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        plannedActivityLabelDao = registerDaoMockFor(PlannedActivityLabelDao.class);
        expect(epochDao.getById(2)).andReturn(e1).anyTimes();
        expect(studySegmentDao.getById(10)).andReturn(e0a0).anyTimes();

        StaticDaoFinder daoFinder = new StaticDaoFinder(epochDao, studySegmentDao, plannedActivityLabelDao);
        MutatorFactory mutatorFactory = new MutatorFactory();
        mutatorFactory.setDaoFinder(daoFinder);
        TestingTemplateService templateService = new TestingTemplateService();
        templateService.setDaoFinder(daoFinder);

        service = new DeltaService();
        service.setMutatorFactory(mutatorFactory);
        service.setTemplateService(templateService);
        service.setChangeDao(changeDao);
        service.setDeltaDao(deltaDao);
        service.setDaoFinder(daoFinder);
        StaticNowFactory nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(new Timestamp(309));
        service.setNowFactory(nowFactory);

        mockTemplateService = registerMockFor(TemplateService.class);
    }

    public void testRevise() throws Exception {
        assertEquals("Wrong number of epochs to start with", 2, calendar.getEpochs().size());

        Amendment inProgress = new Amendment();
        Epoch newEpoch = setGridId("E-NEW", setId(8, Epoch.create("Long term")));
        inProgress.addDelta(Delta.createDeltaFor(calendar, createAddChange(8, null)));

        expect(epochDao.getById(8)).andReturn(newEpoch).anyTimes();

        replayMocks();
        Study revised = service.revise(study, inProgress);
        verifyMocks();

        assertEquals("Epoch not added", 3, revised.getPlannedCalendar().getEpochs().size());
        assertEquals("Epoch not added in the expected location", 8,
            (int) revised.getPlannedCalendar().getEpochs().get(2).getId());

        assertEquals("Original calendar modified", 2, calendar.getEpochs().size());
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
            Add.create(epoch, 3));
        rev.addDelta(pcDelta);
        StudySegment studySegment = createNamedInstance("N", StudySegment.class);

        assertEquals("Wrong number of study segments initially", 1, epoch.getStudySegments().size());

        service.updateRevision(rev, epoch, Add.create(studySegment));

        assertEquals("Study segment not directly applied", 2, epoch.getStudySegments().size());
    }

    public void testUpdateRevisionWhenThereIsAlreadyAnApplicableDelta() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, createNamedInstance("New", Epoch.class));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            Add.create(epoch, 2));
        rev.addDelta(pcDelta);

        assertEquals("Wrong number of changes in delta initially", 1, pcDelta.getChanges().size());

        service.updateRevision(rev, study.getPlannedCalendar(),
            Remove.create(study.getPlannedCalendar().getEpochs().get(1)));

        assertEquals("New change not merged into delta", 2, pcDelta.getChanges().size());
    }
    
    public void testUpdateRevisionWhenThereIsNoApplicableDelta() throws Exception {
        Amendment rev = new Amendment();
        Epoch epoch = setId(4, createNamedInstance("New", Epoch.class));
        Delta<?> pcDelta = Delta.createDeltaFor(study.getPlannedCalendar(),
            Add.create(epoch, 3));
        rev.addDelta(pcDelta);

        assertEquals("Wrong number of deltas initially", 1, rev.getDeltas().size());

        Epoch expectedTarget = study.getPlannedCalendar().getEpochs().get(1);
        PlanTreeNode<?> newChild = new StudySegment();
        Add expectedChange = Add.create(newChild, 2);
        service.updateRevision(rev, expectedTarget, expectedChange);

        assertEquals("Wrong number of deltas", 2, rev.getDeltas().size());
        Delta<?> added = rev.getDeltas().get(1);
        assertSame("Added delta is not for correct node", expectedTarget, added.getNode());
        assertEquals("Wrong number of changes in new delta", 1, added.getChanges().size());
        assertEquals("Wrong change in new delta", expectedChange, added.getChanges().get(0));
    }

    public void testUpdateRevisionForStudy() throws Exception {
        Amendment rev = new Amendment();
        Population population = Fixtures.createPopulation("new abbr ", "new name");
        Change change = Add.create(population, 3);
        Delta<?> studyDelta = Delta.createDeltaFor(study, change);
        rev.addDelta(studyDelta);
        assertEquals("Wrong number of changes in delta initially", 1, studyDelta.getChanges().size());

        service.updateRevision(rev, study, change);

        assertEquals("New change not merged into delta", 1, studyDelta.getChanges().size());
    }

    public void testUpdateRevisionForStudyWithExistingDelta() throws Exception {
        Amendment rev = new Amendment();
        Population population = Fixtures.createPopulation("new abbr ", "new name");
        Change change = Add.create(population, 3);
        Delta<?> studyDelta = Delta.createDeltaFor(study, change);
        rev.addDelta(studyDelta);

        Change propertyChange = PropertyChange.create("name", population.getName(), population.getName()+ " new");
        Delta<?> studyDeltaForPropChange = Delta.createDeltaFor(population, propertyChange);
        rev.addDelta(studyDeltaForPropChange);

        assertEquals("Wrong number of changes in delta initially", 1, studyDelta.getChanges().size());

        service.updateRevision(rev, study, change);

        assertEquals("New change not merged into delta", 1, studyDelta.getChanges().size());
    }

    public void testDeleteDeltaWithRealizedChildInAdd() throws Exception {
        service.setTemplateService(mockTemplateService);

        Epoch addedEpoch = new Epoch();
        Delta<?> pcDelta = Delta.createDeltaFor(calendar, Add.create(addedEpoch), Remove.create(e1));
        mockTemplateService.delete(addedEpoch);
        deltaDao.delete(pcDelta);
        changeDao.delete(pcDelta.getChanges().get(0));
        changeDao.delete(pcDelta.getChanges().get(1));

        replayMocks();
        service.delete(pcDelta);
        verifyMocks();
    }

    public void testDeleteDeltaWithChildIdOnlyInAdd() throws Exception {
        service.setTemplateService(mockTemplateService);

        Epoch addedEpoch = setId(4, new Epoch());
        Delta<?> pcDelta = Delta.createDeltaFor(calendar, createAddChange(4, null), Remove.create(e1));
        expect(epochDao.getById(4)).andReturn(addedEpoch);
        mockTemplateService.delete(addedEpoch);
        deltaDao.delete(pcDelta);
        changeDao.delete(pcDelta.getChanges().get(0));
        changeDao.delete(pcDelta.getChanges().get(1));

        replayMocks();
        service.delete(pcDelta);
        verifyMocks();
    }

    public void testMutateNodeForPlannedActivityLabel() throws Exception {
        ActivityType at = setId(5, Fixtures.createActivityType("Other"));
        Activity a =  setId(5, Fixtures.createActivity("survival-progression"));
        a.setType(at);
        PlannedActivity pa = Fixtures.createPlannedActivity(a, 6);
        pa.setDetails(null);
        PlannedActivityLabel pal1 = setId(3, Fixtures.createPlannedActivityLabel("a"));
        PlannedActivityLabel pal2 = setId(4, Fixtures.createPlannedActivityLabel("b"));
        pa.addPlannedActivityLabel(pal1);
        pa.addPlannedActivityLabel(pal2);

        Change removeChange = Remove.create(pal2);
        replayMocks();
        service.mutateNode(pa, removeChange);
        verifyMocks();

        assertEquals("Planned activity only has one label ", 1, pa.getChildren().size());
    }
}
