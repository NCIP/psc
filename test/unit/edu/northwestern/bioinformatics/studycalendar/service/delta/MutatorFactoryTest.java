package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

/**
 * @author Rhett Sutphin
 */
public class MutatorFactoryTest extends StudyCalendarTestCase {
    private MutatorFactory factory;
    private EpochDao epochDao;
    private ArmDao armDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new MutatorFactory();

        DaoFinder finder = new StaticDaoFinder(
            epochDao = registerDaoMockFor(EpochDao.class),
            armDao = registerDaoMockFor(ArmDao.class)
        );

        factory.setDaoFinder(finder);
    }

    public void testCreateAdderWithIndex() throws Exception {
        Add add = new Add();
        add.setIndex(2);

        Mutator actual = factory.createMutator(new Epoch(), add);
        assertNotNull(actual);
        assertEquals(ListAddMutator.class, actual.getClass());
        assertEquals("Dao does not match child class", armDao, ((CollectionAddMutator) actual).getDao());
    }
    
    public void testCreateAdderWithoutIndex() throws Exception {
        Add add = new Add();
        add.setIndex(null);

        Mutator actual = factory.createMutator(new PlannedCalendar(), add);
        assertNotNull(actual);
        assertEquals(CollectionAddMutator.class, actual.getClass());
        assertEquals("Dao does not match child class", epochDao, ((CollectionAddMutator) actual).getDao());
    }
}
