package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodCountEditorTest extends StudyCalendarTestCase {
    private ManagePeriodCountEditor editor;

    protected void setUp() throws Exception {
        super.setUp();
        editor = new ManagePeriodCountEditor();
    }

    public void testSetBasicNumbers() throws Exception {
        editor.setAsText("1");
        assertEquals(1, editor.getValue());
        editor.setAsText("2");
        assertEquals(2, editor.getValue());
    }
    
    public void testGetBasicNumbers() throws Exception {
        editor.setValue(1);
        assertEquals("1", editor.getAsText());
        editor.setValue(2);
        assertEquals("2", editor.getAsText());
    }

    public void testSetBlankIsZero() throws Exception {
        editor.setAsText("   ");
        assertEquals(0, editor.getValue());
    }

    public void testSetOtherCharsIsOne() throws Exception {
        editor.setAsText("X");
        assertEquals(1, editor.getValue());
        editor.setAsText("x");
        assertEquals(1, editor.getValue());
    }
}
