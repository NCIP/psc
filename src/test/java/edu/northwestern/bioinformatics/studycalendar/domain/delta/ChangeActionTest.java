package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ChangeActionTest extends StudyCalendarTestCase {
    public void testCodes() throws Exception {
        assertEquals("add",      ChangeAction.ADD.getCode());
        assertEquals("remove",   ChangeAction.REMOVE.getCode());
        assertEquals("reorder",  ChangeAction.REORDER.getCode());
        assertEquals("property", ChangeAction.CHANGE_PROPERTY.getCode());
    }

    public void testGetByCode() throws Exception {
        assertSame(ChangeAction.ADD,             ChangeAction.getByCode("add"));
        assertSame(ChangeAction.REMOVE,          ChangeAction.getByCode("remove"));
        assertSame(ChangeAction.REORDER,         ChangeAction.getByCode("reorder"));
        assertSame(ChangeAction.CHANGE_PROPERTY, ChangeAction.getByCode("property"));
    }
    
    public void testDisplayNames() throws Exception {
        assertEquals("Add",             ChangeAction.ADD.getDisplayName());
        assertEquals("Remove",          ChangeAction.REMOVE.getDisplayName());
        assertEquals("Reorder",         ChangeAction.REORDER.getDisplayName());
        assertEquals("Change property", ChangeAction.CHANGE_PROPERTY.getDisplayName());
    }
}
