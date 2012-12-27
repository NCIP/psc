/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class ChangeActionTest extends TestCase {
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
