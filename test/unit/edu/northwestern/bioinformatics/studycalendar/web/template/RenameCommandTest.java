package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import org.easymock.classextension.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class RenameCommandTest extends StudyCalendarTestCase {
    private static final String NEW_NAME = "new name";
    private static final Study STUDY = new Study();
    private StudyDao studyDao;
    private RenameCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        command = new RenameCommand(studyDao);
        command.setValue(NEW_NAME);
    }

    public void testRenameStudy() throws Exception {
        command.setStudy(STUDY);

        doApply();
        assertRenamed("Study", STUDY);
    }

    public void testRenameMultiArmEpoch() throws Exception {
        Epoch epoch = createEpoch("E1", "A", "B");
        command.setEpoch(epoch);

        doApply();
        assertRenamed("Epoch", epoch);
        assertName("Arm 0 should not be renamed", "A", epoch.getArms().get(0));
        assertName("Arm 1 should not be renamed", "B", epoch.getArms().get(1));
    }

    public void testRenameNoArmEpoch() throws Exception {
        Epoch epoch = createEpoch("E1");
        command.setEpoch(epoch);

        doApply();
        assertRenamed("Epoch", epoch);
        assertRenamed("Sole arm", epoch.getArms().get(0));
    }

    public void testRenameArmFromMultiArmEpoch() throws Exception {
        Epoch epoch = createEpoch("E1", "A", "B");
        command.setArm(epoch.getArms().get(0));

        doApply();
        assertName("Epoch should not be renamed", "E1", epoch);
        assertRenamed("Arm 0", epoch.getArms().get(0));
        assertName("Arm 1 should not be renamed", "B", epoch.getArms().get(1));
    }

    public void testRenameArmOfNoArmEpoch() throws Exception {
        Epoch epoch = createEpoch("E1");
        command.setArm(epoch.getArms().get(0));

        doApply();
        assertRenamed("Epoch", epoch);
        assertRenamed("Sole arm", epoch.getArms().get(0));
    }
    
    public void testRenameAll() throws Exception {
        Epoch epoch = createEpoch("E1", "A", "B");
        command.setStudy(STUDY);
        command.setEpoch(epoch);
        command.setArm(epoch.getArms().get(1));

        doApply();
        assertRenamed("Study", STUDY);
        assertRenamed("Epoch", epoch);
        assertName("Arm 0 should not be renamed", "A", epoch.getArms().get(0));
        assertRenamed("Arm", epoch.getArms().get(1));
    }

    private static Epoch createEpoch(String epochName, String... armNames) {
        PlannedCalendar cal = new PlannedCalendar();
        cal.setStudy(STUDY);

        Epoch epoch = Fixtures.createEpoch(epochName, armNames);
        epoch.setPlannedCalendar(cal);
        return epoch;
    }

    private void doApply() {
        studyDao.save(STUDY);
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
