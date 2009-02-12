package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.RoleEditor;

/**
 * @author Rhett Sutphin
 */
public class RoleEditorTest extends StudyCalendarTestCase {
    private RoleEditor editor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editor = new RoleEditor();
    }

    public void testSetAsTextLooksUpByCode() throws Exception {
        editor.setAsText(Role.STUDY_ADMIN.getCode());
        assertEquals(Role.STUDY_ADMIN, editor.getValue());
    }

    public void testSetAsTextBlankGivesNull() throws Exception {
        editor.setAsText("\n \t\t");
        assertNull(editor.getValue());
    }
    
    public void testSetAsTextNullGivesNull() throws Exception {
        editor.setAsText(null);
        assertNull(editor.getValue());
    }

    public void testGetAsTextGetsCode() throws Exception {
        editor.setValue(Role.STUDY_COORDINATOR);
        assertEquals(Role.STUDY_COORDINATOR.getCode(), editor.getAsText());
    }
    
    public void testGetAsTextGetsNullForNullValue() throws Exception {
        editor.setValue(null);
        assertNull(editor.getAsText());
    }
}
