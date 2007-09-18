package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Moses Hohman
 */
public class ArmDaoTest extends DaoTestCase {
    private ArmDao dao = (ArmDao) getApplicationContext().getBean("armDao");

    public void testGetByIdAndTheHibernateMapping() {
        Arm arm = dao.getById(-2);
        assertNotNull("arm not found", arm);
        assertEquals("name wrong", "Treatment", arm.getName());
        assertEquals("wrong number of periods", 2, arm.getPeriods().size());
        Map<String, Period> periodsByName = getPeriodsByName(arm);
        assertTrue("Pre-screening period not present", periodsByName.containsKey("Pre-screening"));
        assertTrue("Treatment period not present", periodsByName.containsKey("Treatment"));

        Period treatment = periodsByName.get("Treatment");
        assertEquals("Wrong arm for treatment period", arm, treatment.getArm());
        assertEquals("Wrong start day for treatment", new Integer(8), treatment.getStartDay());
        assertEquals("Wrong duration for treatment", new Duration(6, Duration.Unit.week), treatment.getDuration());
        assertEquals("Wrong repetitions for treatment", 3, treatment.getRepetitions());
    }

    private Map<String, Period> getPeriodsByName(Arm arm) {
        Map<String, Period> periodsByName = new HashMap<String, Period>();
        for(Period period : arm.getPeriods()) {
            periodsByName.put(period.getName(), period);
        }
        return periodsByName;
    }

    public void testUpdateArm() throws Exception {
        {
            Arm arm = dao.getById(-2);
            Period newPeriod = new Period();
            newPeriod.setName("Checkup");
            newPeriod.setStartDay(1);
            newPeriod.setDuration(new Duration(7, Duration.Unit.day));
            newPeriod.setRepetitions(1);
            arm.addPeriod(newPeriod);

            dao.save(arm);
        }

        interruptSession();

        {
            Arm loaded = dao.getById(-2);
            assertEquals("Wrong number of periods", 3, loaded.getPeriods().size());
            Map<String, Period> periodsByName = getPeriodsByName(loaded);
            assertTrue("Checkup period not found", periodsByName.containsKey("Checkup"));
            Period checkup = periodsByName.get("Checkup");
            assertEquals("Wrong duration for checkup period", new Duration(7, Duration.Unit.day), checkup.getDuration());
            assertEquals("Wrong repetitions for checkup period", 1, checkup.getRepetitions());
        }
    }
    
    public void testSaveArmWithoutEpoch() throws Exception {
        Integer id;
        {
            Arm arm = new Arm();
            arm.setName("Hula");

            dao.save(arm);
            assertNotNull("Not saved", arm.getId());
            id = arm.getId();
        }

        interruptSession();

        {
            Arm loaded = dao.getById(id);
            assertEquals("Wrong arm", "Hula", loaded.getName());
        }
    }

    public void testDetachArmFromEpoch() throws Exception {
        {
            Arm arm = dao.getById(-2);
            arm.getParent().removeChild(arm);
            dao.save(arm);
        }

        interruptSession();

        Arm reloaded = dao.getById(-2);
        assertNull(reloaded.getEpoch());
    }
}
