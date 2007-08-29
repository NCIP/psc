package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;

import java.util.List;

public class ChangeDaoTest extends DaoTestCase {
    private ChangeDao changeDao = (ChangeDao) getApplicationContext().getBean("changeDao");

//    <CHANGES
//        id="-6"
//        action="add"
//        old_value="a"
//        new_value="b"
//        attribute="c"
//        delta_id='-6'
//    />
//    <CHANGES
//        id="-4"
//        action="add"
//        old_value="d"
//        new_value="e"
//        attribute="f"
//        delta_id="-4"
//    />

    public void testGetByChangeId() throws Exception {
        Change change = changeDao.getById(-4);
        assertNotNull("Change was not found", change);
        assertEquals("Wrong change action", "add", change.getAction().getCode());
    }

    public void testGetAllChanges() throws Exception {
        List<Change> changes = changeDao.getAll();
        assertNotNull("Changes not found", changes);
        assertEquals("Wrong amount of changes ", 2, changes.size());
    }
    
    public void testSaveAddChange() throws Exception {
//        Add addChange = new Add();
//        addChange.setId(-55);
//        addChange.setIndex(-14);
//        addChange.setNewChildId(-3);
//        List<Change> beforeSave = changeDao.getAll();
//        changeDao.save(addChange);
//        List<Change> afterSave = changeDao.getAll();
//        assertEquals("Change wasn't inserted ", afterSave.size(), beforeSave.size()+1);
    }
}
