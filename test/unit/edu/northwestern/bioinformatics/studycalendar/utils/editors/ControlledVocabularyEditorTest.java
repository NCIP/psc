package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.AbstractControlledVocabularyObject;
import junit.framework.Test;

/**
 * @author Rhett Sutphin
 */
public class ControlledVocabularyEditorTest extends StudyCalendarTestCase {
    private ControlledVocabularyEditor editor
        = new ControlledVocabularyEditor(TestVocab.class);

    public void testGetAsText() throws Exception {
        editor.setValue(TestVocab.TWO);
        assertEquals("2", editor.getAsText());
    }

    public void testSetAsText() throws Exception {
        editor.setAsText("3");
        assertSame(TestVocab.THREE, editor.getValue());
    }

    public void testSetAsTextNull() throws Exception {
        editor.setAsText(null);
        assertNull(editor.getValue());
    }

    public void testGetAsTextWhenNull() throws Exception {
        editor.setValue(null);
        assertNull(editor.getAsText());
    }

    private static class TestVocab extends AbstractControlledVocabularyObject {
        public static final TestVocab ONE   = new TestVocab(1, "one");
        public static final TestVocab TWO   = new TestVocab(2, "two");
        public static final TestVocab THREE = new TestVocab(3, "three");

        private TestVocab(int id, String name) { super(id, name); }

        public static TestVocab getById(int id) {
            return getById(TestVocab.class, id);
        }
    }
}
