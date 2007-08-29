package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.EpochDeltaDao;

import java.util.List;
import java.util.ArrayList;


public class DeltaDaoTest extends DaoTestCase {
    private DeltaDao deltaDao = (DeltaDao) getApplicationContext().getBean("deltaDao");
//    private EpochDeltaDao epochDeltaDao = (EpochDeltaDao) getApplicationContext().getBean("epochDeltaDao");


//    <DELTAS
//        id="-100"
//        discriminator_id="1"
//        change_id="-4"
//        node_id="-1"
//        amendment_id="-1"
//
//        />
//    <DELTAS
//         id="-200"
//         discriminator_id="1"
//         change_id="-6"
//         node_id="-1"
//         amendment_id="-1"
//        />
//    <CHANGES
//        id="-6"
//        action="add"
//        old_value="-1"
//        new_value="-2"
//        attribute="-3"
//        delta_id="-100"
//    />
//    <CHANGES
//        id="-4"
//        action="add"
//        old_value="-1"
//        new_value="-2"
//        attribute="-4"
//        delta_id="-100"
//    />
//    <STUDIES id="-2" name="Protocol"/>
//    <PLANNED_CALENDARS id="-2" study_id="-2"/>
//    <EPOCHS
//        id="-1"
//        planned_calendar_id="-2"
//        name="Treatment"
//        />


    public void testGetByChangeIdOne() throws Exception {
        Delta actual = deltaDao.getById(-100);
        List<Change> changes = actual.getChanges();
        Change change = changes.get(0);
        if (change instanceof Add) {
            Add addChange = (Add)change;
            assertEquals("Wrong change index ", -3, (int) addChange.getIndex());
            assertEquals("Wrong change index ", -2, (int) addChange.getNewChildId());
        }
        assertNotNull("Delta was not found", actual);
        assertEquals("Changes not found", 2, changes.size());
        assertEquals("Wrong change action", "add", change.getAction().getCode());

    }

    public void testGetByChangeId() throws Exception {
        Delta actual = deltaDao.getById(-100);
        assertNotNull("Delta was not found", actual);
    }

    public void testGetChanges() throws Exception {
        Delta actual = deltaDao.getById(-100);
        List<Change> changes = actual.getChanges();
        assertEquals("Changes not found", 2, changes.size());
        Change changeOne = changes.get(0);
        assertEquals("Wrong change action", "add", changeOne.getAction().getCode());
        if (changeOne instanceof Add) {
            Add addChange = (Add)changeOne;
            assertEquals("Wrong change index ", -3, (int) addChange.getIndex());
            assertEquals("Wrong change newChildId ", -2, (int) addChange.getNewChildId());
        }
        Change changeTwo = changes.get(1);
        assertEquals("Wrong change action", "add", changeTwo.getAction().getCode());
        if (changeTwo instanceof Add) {
            Add addChange = (Add)changeTwo;
            assertEquals("Wrong change index ", -4, (int) addChange.getIndex());
            assertEquals("Wrong change newChildId ", -2, (int) addChange.getNewChildId());
        }        
    }

    public void testGetNode() throws Exception {
        Delta actual = deltaDao.getById(-100);
        PlanTreeNode<Epoch> node = actual.getNode();
        assertEquals("Node is not found", -1, (int) node.getId());
    }

    public void testGetAllDeltas() throws Exception {
        List<Delta> deltas = deltaDao.getAll();
        assertEquals("Deltas are null", 2, deltas.size());
    }

    public void testSetDelta() throws Exception {
//        Add change = new Add();
//        change.setId(1);
//        change.setIndex(2);
//        change.setNewChildId(3);
//        change.setOldValue("4");
//        List<Change> changeList = new ArrayList();
//        changeList.add(change);
//        Delta delta = new EpochDelta();
//        delta.setChanges(changeList);
//        delta.setNode(new Epoch());
//        delta.setId(22);
//        deltaDao.save(delta);
//        System.out.println("===here?");
    }

}
