package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

/**
 * @author Rhett Sutphin
 */
public class EpochDaoTest extends ContextDaoTestCase<EpochDao> {

    protected String getTestDataFileName() {
        return "testdata/ArmDaoTest.xml";
    }
    
    public void testGetById() throws Exception {
        Epoch loaded = getDao().getById(-1);
        assertEquals("Wrong name", "Epoch", loaded.getName());
        assertEquals("Wrong number of arms", 1, loaded.getArms().size());
        assertEquals("Wrong planned calendar", -1, (int) loaded.getPlannedCalendar().getId());
    }
}
