package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class StudyTest extends TestCase {
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
}
