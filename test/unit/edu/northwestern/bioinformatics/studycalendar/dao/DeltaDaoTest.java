package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import java.util.List;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

public class DeltaDaoTest extends DaoTestCase {
    private DeltaDao deltaDao = (DeltaDao) getApplicationContext().getBean("deltaDao");
    private ArmDao armDao = (ArmDao) getApplicationContext().getBean("armDao");
    private PeriodDao periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");

    public void testLoadAddChanges() throws Exception {
        Delta<?> actual = deltaDao.getById(-100);
        assertNotNull("Delta was not found", actual);
        List<Change> changes = actual.getChanges();
        assertEquals("Changes not found", 2, changes.size());
        assertNotNull("? " + changes, changes.get(0));
        assertEquals("Wrong change action", ChangeAction.ADD, changes.get(0).getAction());
        assertTrue("Wrong change subtype", changes.get(0) instanceof Add);
        Add addOne = (Add) changes.get(0);
        assertEquals("Wrong change index ", 1, (int) addOne.getIndex());
        assertEquals("Wrong change newChildId ", -3, (int) addOne.getChildId());

        Change changeTwo = changes.get(1);
        assertEquals("Wrong change action", ChangeAction.ADD, changeTwo.getAction());
        assertTrue("Wrong change subtype", changeTwo instanceof Add);
        Add addTwo = (Add)changeTwo;
        assertEquals("Wrong change index ", 0, (int) addTwo.getIndex());
        assertEquals("Wrong change newChildId ", -2, (int) addTwo.getChildId());
    }

    public void testLoadReorder() throws Exception {
        Delta<?> actualDelta = deltaDao.getById(-200);
        List<Change> changes = actualDelta.getChanges();
        assertEquals("Changes not found", 2, changes.size());
        Change actual = changes.get(0);
        assertEquals("Wrong change action", ChangeAction.REORDER, actual.getAction());
        assertTrue("Wrong change subtype", actual instanceof Reorder);
        Reorder reorder = (Reorder) actual;
        assertEquals("Wrong child to move", -2, (int) reorder.getChildId());
        assertEquals("Wrong new index", 0, (int) reorder.getNewIndex());
    }

    public void testLoadRemove() throws Exception {
        Delta<?> actualDelta = deltaDao.getById(-200);
        List<Change> changes = actualDelta.getChanges();
        assertEquals("Changes not found", 2, changes.size());
        Change actual = changes.get(1);
        assertEquals("Wrong change action", ChangeAction.REMOVE, actual.getAction());
        assertTrue("Wrong change subtype", actual instanceof Remove);
        Remove remove = (Remove) actual;
        assertEquals("Wrong child id", -3, (int) remove.getChildId());
    }

    public void testLoadPropertyChange() throws Exception {
        Delta<?> actualDelta = deltaDao.getById(-210);
        List<Change> changes = actualDelta.getChanges();
        assertEquals("Changes not found", 1, changes.size());
        Change actual = changes.get(0);
        assertEquals("Wrong change action", ChangeAction.CHANGE_PROPERTY, actual.getAction());
        assertTrue("Wrong change subtype", actual instanceof PropertyChange);
        PropertyChange prop = (PropertyChange) actual;
        assertEquals("Wrong old value", "7", prop.getOldValue());
        assertEquals("Wrong new value", "4", prop.getNewValue());
        assertEquals("Wrong property", "day", prop.getPropertyName());
    }

    public void testGetNode() throws Exception {
        Delta<?> actual = deltaDao.getById(-100);
        assertEquals("Wrong node", -1, (int) actual.getNode().getId());
    }

    public void testGetPlannedEventDelta() throws Exception {
        Delta<?> actual = deltaDao.getById(-210);
        assertTrue("Delta's node is not a planned event",
            PlannedEvent.class.isAssignableFrom(actual.getNode().getClass()));
    }

    public void testFindOriginalAddDeltaForArm() throws Exception {
        Arm arm = armDao.getById(-3);
        assertNotNull("Test setup failure", arm);
        Delta<Epoch> found = deltaDao.findDeltaWhereAdded(arm);
        assertNotNull("Delta not found", found);
        assertEquals("Wrong delta found", -100, (int) found.getId());
    }

    public void testFindOriginalAddDeltaForPeriodWithSameId() throws Exception {
        Period period = periodDao.getById(-3);
        assertNotNull("Test setup failure", period);
        Delta<Arm> found = deltaDao.findDeltaWhereAdded(period);
        assertNotNull("Delta not found", found);
        assertEquals("Wrong delta found", -220, (int) found.getId());
    }
}
