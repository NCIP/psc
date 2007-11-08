package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

/**
 * @author Rhett Sutphin
 */
public class MutatorFactoryTest extends StudyCalendarTestCase {
    private MutatorFactory factory;
    private EpochDao epochDao;
    private ArmDao armDao;
    private PeriodDao periodDao;
    private PlannedActivityDao plannedActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new MutatorFactory();

        DaoFinder finder = new StaticDaoFinder(
            epochDao = registerDaoMockFor(EpochDao.class),
            armDao = registerDaoMockFor(ArmDao.class),
            periodDao = registerDaoMockFor(PeriodDao.class),
            plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class)
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

    public void testCreatePeriodAdder() throws Exception {
        Add add = new Add();
        add.setChildId(5);
        Mutator actual = factory.createMutator(new Arm(), add);
        assertNotNull(actual);
        assertEquals(AddPeriodMutator.class,  actual.getClass());
    }
    
    public void testCreatePlannedEventAdder() throws Exception {
        Add add = new Add();
        add.setIndex(8);
        add.setChildId(5);
        Mutator actual = factory.createMutator(new Period(), add);
        assertNotNull(actual);
        assertEquals(AddPlannedEventMutator.class,  actual.getClass());
    }

    public void testCreatePropertyMutator() throws Exception {
        Mutator actual = factory.createMutator(new Arm(), new PropertyChange());
        assertNotNull(actual);
        assertEquals(SimplePropertyChangeMutator.class, actual.getClass());
    }

    public void testCreatePeriodStartMutator() throws Exception {
        Mutator actual = factory.createMutator(new Period(), PropertyChange.create("startDay", "11", "8"));
        assertNotNull(actual);
        assertEquals(ChangePeriodStartDayMutator.class, actual.getClass());
    }

    public void testCreatePeriodRepsMutator() throws Exception {
        Mutator actual = factory.createMutator(new Period(), PropertyChange.create("repetitions", "4", "8"));
        assertNotNull(actual);
        assertEquals(ChangePeriodRepetitionsMutator.class, actual.getClass());
    }

    public void testCreatePeriodDurationQuantityMutator() throws Exception {
        Mutator actual = factory.createMutator(new Period(), PropertyChange.create("duration.quantity", "4", "8"));
        assertNotNull(actual);
        assertEquals(ChangePeriodDurationQuantityMutator.class, actual.getClass());
    }

    public void testCreatePeriodDurationUnitMutator() throws Exception {
        Mutator actual = factory.createMutator(new Period(), PropertyChange.create("duration.unit", "day", "week"));
        assertNotNull(actual);
        assertEquals(ChangePeriodDurationUnitMutator.class, actual.getClass());
    }

    public void testCreatePlannedEventDetailsMutator() throws Exception {
        Mutator actual = factory.createMutator(new PlannedActivity(), PropertyChange.create("details", "F", "Fprime"));
        assertNotNull(actual);
        assertEquals(ChangePlannedEventSimplePropertyMutator.class, actual.getClass());
    }

    /* TODO: pending
    public void testCreatePlannedEventConditionMutator() throws Exception {
        Mutator actual = factory.createMutator(new PlannedActivity(), PropertyChange.create("condition", "F", "Fprime"));
        assertNotNull(actual);
        assertEquals(ChangePlannedEventSimplePropertyMutator.class, actual.getClass());
    }
    */

    public void testCreatePlannedEventDayMutator() throws Exception {
        Mutator actual = factory.createMutator(new PlannedActivity(), PropertyChange.create("day", "4", "18"));
        assertNotNull(actual);
        assertEquals(ChangePlannedEventDayMutator.class, actual.getClass());
    }

    public void testCreateReorderMutator() throws Exception {
        Mutator actual = factory.createMutator(new Epoch(), new Reorder());
        assertNotNull(actual);
        assertEquals(ReorderMutator.class, actual.getClass());
    }
    
    public void testCreateRemoveMutator() throws Exception {
        Mutator actual = factory.createMutator(new Epoch(), new Remove());
        assertNotNull(actual);
        assertEquals(RemoveMutator.class, actual.getClass());
        assertEquals("Dao does not match child class", armDao, ((RemoveMutator) actual).getDao());
    }

    public void testCreateRemovePeriodMutator() throws Exception {
        Remove remove = new Remove();
        remove.setChildId(5);
        Mutator actual = factory.createMutator(new Arm(), remove);
        assertNotNull(actual);
        assertEquals(RemovePeriodMutator.class,  actual.getClass());
    }
    
    public void testCreateRemovePlannedEventMutator() throws Exception {
        Remove remove = new Remove();
        remove.setChildId(5);
        Mutator actual = factory.createMutator(new Period(), remove);
        assertNotNull(actual);
        assertEquals(RemovePlannedEventMutator.class,  actual.getClass());
    }
}
