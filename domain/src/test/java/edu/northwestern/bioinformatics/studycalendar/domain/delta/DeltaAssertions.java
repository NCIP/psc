/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import static junit.framework.Assert.*;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class DeltaAssertions {
    public static void assertReorder(String msg, PlanTreeNode<?> expectedChild, int expectedOld, int expectedNew, Change actual) {
        assertEquals(msg + ": not reorder", ChangeAction.REORDER, actual.getAction());
        assertTrue(msg + ": not reorder", actual instanceof Reorder);
        Reorder actualReorder = (Reorder) actual;
        assertEquals(msg + ": wrong child", expectedChild, actualReorder.getChild());
        assertEquals(msg + ": wrong new index", expectedNew, (int) actualReorder.getNewIndex());
        assertEquals(msg + ": wrong old index", expectedOld, (int) actualReorder.getOldIndex());
    }

    public static void assertRemove(String msg, PlanTreeNode<?> expectedChild, Change actual) {
        assertEquals(msg + ": not remove", ChangeAction.REMOVE, actual.getAction());
        assertTrue(msg + ": not remove", actual instanceof Remove);
        Remove actualRemove = (Remove) actual;
        assertEquals(msg + ": wrong child", expectedChild, actualRemove.getChild());
    }

    public static void assertAdd(
        String msg, PlanTreeNode<?> expectedChild, Integer expectedIndex, Change actual
    ) {
        Add actualAdd = assertChangeIsAdd(msg, actual);
        assertEquals(msg + ": wrong child", expectedChild, actualAdd.getChild());
        assertEquals(msg + ": wrong index", expectedIndex, actualAdd.getIndex());
    }

    public static Add assertChangeIsAdd(String msg, Change actual) {
        assertEquals(msg + ": not add", ChangeAction.ADD, actual.getAction());
        assertTrue(msg + ": not add", actual instanceof Add);
        return (Add) actual;
    }

    public static void assertPropertyChange(
        String msg, String expectedProperty, String expectedOld, String expectedNew, Change actual
    ) {
        assertEquals(msg + ": not property change", ChangeAction.CHANGE_PROPERTY, actual.getAction());
        assertTrue(msg + ": not property change", actual instanceof PropertyChange);
        PropertyChange actualChange = (PropertyChange) actual;
        assertEquals(msg + ": wrong property", expectedProperty, actualChange.getPropertyName());
        assertEquals(msg + ": wrong old value", expectedOld, actualChange.getOldValue());
        assertEquals(msg + ": wrong new value", expectedNew, actualChange.getNewValue());
    }

    public static void assertChangeTime(String msg, Date expected, Change actual) {
        assertEquals(msg, expected, actual.getUpdatedDate());
    }
}
