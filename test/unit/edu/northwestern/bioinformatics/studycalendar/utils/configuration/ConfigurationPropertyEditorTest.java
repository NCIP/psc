package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationPropertyEditorTest extends StudyCalendarTestCase {
    private ConfigurationPropertyEditor<List<String>> editor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editor = new ConfigurationPropertyEditor<List<String>>(Configuration.MAIL_EXCEPTIONS_TO);
    }

    @SuppressWarnings("unchecked")
    public void testSetText() throws Exception {
        editor.setAsText("a, b, c, d");

        assertTrue(editor.getValue() instanceof List);
        List<String> actual = ((List<String>) editor.getValue());
        assertEquals("Wrong number of values", 4, actual.size());
        assertEquals("Wrong value 0", "a", actual.get(0));
        assertEquals("Wrong value 1", "b", actual.get(1));
        assertEquals("Wrong value 2", "c", actual.get(2));
        assertEquals("Wrong value 3", "d", actual.get(3));
    }
    
    public void testGetText() throws Exception {
        editor.setValue(Arrays.asList("g", "h", "i"));
        assertEquals("g, h, i", editor.getAsText());
    }
    
    public void testGetTextWhenNull() throws Exception {
        editor.setValue(null);
        assertNull(editor.getAsText());
    }
}
