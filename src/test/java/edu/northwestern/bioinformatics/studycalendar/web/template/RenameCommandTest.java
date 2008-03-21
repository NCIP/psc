package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class RenameCommandTest extends EditCommandTestCase {
    private static final String NEW_NAME = "new name";
    private StudyService studyService;
    private RenameCommand command;
    private StudyDao studyDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        command = new RenameCommand();
        command.setStudyService(studyService);
        expect(studyService.getStudyDao()).andReturn(studyDao).anyTimes();
        command.setDeltaService(getTestingDeltaService());
        command.setValue(NEW_NAME);

        // study.getPlannedCalendar().addEpoch(Epoch.create("E1"));
        assignIds(study);
        command.setStudy(study);
    }

    public void testRenameStudy() throws Exception {
        expect(studyDao.getByAssignedIdentifier(NEW_NAME)).andReturn(null);
        command.setStudy(study);

        doApply();
        assertRenamed("Study", study);
    }

    public void testRenameMultiStudySegmentEpoch() throws Exception {
        Epoch epoch = createAndAddEpoch("E1", "A", "B");
        command.setEpoch(epoch);

        doApply();
        assertEquals("Wrong number of deltas", 1, study.getDevelopmentAmendment().getDeltas().size());
        assertSame("Delta is for wrong node", epoch, lastDelta().getNode());
        assertPropertyChange("Epoch not renamed", "name", "E1", NEW_NAME, lastChange());
    }

    public void testRenameNoStudySegmentEpoch() throws Exception {
        Epoch epoch = createAndAddEpoch("E1");
        command.setEpoch(epoch);

        doApply();
        assertEquals("Wrong number of deltas", 2, study.getDevelopmentAmendment().getDeltas().size());
        assertPropertyChange("Epoch not renamed", "name", "E1", NEW_NAME,
            study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0));
        assertPropertyChange("Sole studySegment not renamed", "name", "E1", NEW_NAME, lastChange());
    }

    public void testRenameStudySegmentFromMultiStudySegmentEpoch() throws Exception {
        Epoch epoch = createAndAddEpoch("E1", "A", "B");
        command.setStudySegment(epoch.getStudySegments().get(0));

        doApply();
        assertEquals("Wrong number of deltas", 1,
            study.getDevelopmentAmendment().getDeltas().size());
        assertEquals("Wrong affected node", epoch.getStudySegments().get(0), lastDelta().getNode());
        assertPropertyChange("StudySegment not renamed", "name", "A", NEW_NAME, lastChange());
    }

    public void testRenameStudySegmentOfNoStudySegmentEpoch() throws Exception {
        Epoch epoch = createAndAddEpoch("E1");
        command.setStudySegment(epoch.getStudySegments().get(0));

        doApply();
        assertEquals("Wrong number of deltas", 2, study.getDevelopmentAmendment().getDeltas().size());
        assertPropertyChange("Sole studySegment not renamed", "name", "E1", NEW_NAME,
            study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0));
        assertPropertyChange("Epoch not renamed", "name", "E1", NEW_NAME, lastChange());
    }
    
    private Epoch createAndAddEpoch(String epochName, String... studySegmentNames) {
        Epoch epoch = setId(20, Epoch.create(epochName, studySegmentNames));
        int studySegmentId = 200;
        for (StudySegment studySegment : epoch.getStudySegments()) studySegment.setId(++studySegmentId);
        study.getPlannedCalendar().addEpoch(epoch);
        return epoch;
    }

    private void doApply() {
        studyService.save(study);
        replayMocks();
        command.apply();
        verifyMocks();
    }

    private static void assertRenamed(String desc, Named named) {
        assertName(desc + " not renamed", NEW_NAME, named);
    }

    private static void assertName(String message, String expectedName, Named named) {
        assertEquals(message, expectedName, named.getName());
    }
}
