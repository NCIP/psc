package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class StudyTest extends StudyCalendarTestCase {
    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        study = new Study();
    }

    public void testAddArm() throws Exception {
        Arm arm = new Arm();
        study.addArm(arm);
        assertEquals("Wrong number of arms", 1, study.getArms().size());
        assertSame("Wrong arm present", arm, study.getArms().get(0));
        assertSame("Bidirectional relationship not maintained", study, arm.getStudy());
    }

    public void testLength() throws Exception {
        Arm a1 = registerMockFor(Arm.class);
        expect(a1.getLengthInDays()).andReturn(45);
        a1.setStudy(study);

        Arm a2 = registerMockFor(Arm.class);
        expect(a2.getLengthInDays()).andReturn(13);
        a2.setStudy(study);

        replayMocks();

        study.addArm(a1);
        study.addArm(a2);
        assertEquals(45, study.getLengthInDays());
        verifyMocks();
    }
}
