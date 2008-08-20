package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.easymock.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class EpochTest extends StudyCalendarTestCase {
    private Epoch epoch;

    protected void setUp() throws Exception {
        super.setUp();
        epoch = new Epoch();
    }

    public void testAddStudySegment() throws Exception {
        StudySegment studySegment = new StudySegment();
        epoch.addStudySegment(studySegment);
        assertEquals("Wrong number of arms", 1, epoch.getStudySegments().size());
        assertSame("Wrong studySegment present", studySegment, epoch.getStudySegments().get(0));
        assertSame("Bidirectional relationship not maintained", epoch, studySegment.getEpoch());
    }

    public void testLength() throws Exception {
        StudySegment a1 = registerMockFor(StudySegment.class);
        EasyMock.expect(a1.getLengthInDays()).andReturn(45);
        a1.setParent(epoch);

        StudySegment a2 = registerMockFor(StudySegment.class);
        EasyMock.expect(a2.getLengthInDays()).andReturn(13);
        a2.setParent(epoch);

        replayMocks();

        epoch.addStudySegment(a1);
        epoch.addStudySegment(a2);
        assertEquals(45, epoch.getLengthInDays());
        verifyMocks();
    }
    
    public void testMultipleStudySegments() throws Exception {
        assertFalse(new Epoch().isMultipleStudySegments());
        assertFalse(Epoch.create("Holocene").isMultipleStudySegments());
        assertTrue(Epoch.create("Holocene", "A", "B").isMultipleStudySegments());
    }

    public void testCreateNoStudySegments() throws Exception {
        Epoch noStudySegments = Epoch.create("Holocene");
        assertEquals("Holocene", noStudySegments.getName());
        assertEquals("Wrong number of study segments", 1, noStudySegments.getStudySegments().size());
        assertEquals("Wrong name for sole study segment", "Holocene", noStudySegments.getStudySegments().get(0).getName());
    }
    
    public void testCreateMultipleStudySegments() throws Exception {
        Epoch studySegmented = Epoch.create("Holocene", "H", "I", "J");
        assertEquals("Holocene", studySegmented.getName());
        assertEquals("Wrong number of arms", 3, studySegmented.getStudySegments().size());
        assertEquals("Wrong name for study segment 0", "H", studySegmented.getStudySegments().get(0).getName());
        assertEquals("Wrong name for study segment 1", "I", studySegmented.getStudySegments().get(1).getName());
        assertEquals("Wrong name for study segment 2", "J", studySegmented.getStudySegments().get(2).getName());
    }
    
    public void testIndexOf() throws Exception {
        Epoch studySegmented = Epoch.create("Holocene", "H", "I", "J");
        assertEquals(0, studySegmented.indexOf(studySegmented.getStudySegments().get(0)));
        assertEquals(2, studySegmented.indexOf(studySegmented.getStudySegments().get(2)));
        assertEquals(1, studySegmented.indexOf(studySegmented.getStudySegments().get(1)));
    }

    public void testIndexOfNonChildThrowsException() throws Exception {
        Epoch e = Epoch.create("E", "A1", "A2");
        StudySegment other = Fixtures.createNamedInstance("A7", StudySegment.class);
        try {
            e.indexOf(other);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals(other + " is not a child of " + e, iae.getMessage());
        }
    }
    
    public void testFindMatchingChildSegmentWhenPresent() throws Exception {
        Epoch e = Epoch.create("E", "A1", "A2");
        assertSame("Not found", e.getChildren().get(1), e.findNaturallyMatchingChild("A2"));
    }

    public void testFindMatchingChildSegmentWhenNotPresent() throws Exception {
        Epoch e = Epoch.create("E", "A1", "A2");
        assertNull(e.findNaturallyMatchingChild("A0"));
    }
}
