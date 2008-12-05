package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;

import java.util.Set;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PopulationDaoTest extends ContextDaoTestCase<PopulationDao> {
    private StudyDao studyDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    }

    public void testGetById() throws Exception {
        Population loaded = getDao().getById(-77);
        assertEquals("Hepatitis positive", loaded.getName());
        assertEquals("H+", loaded.getAbbreviation());
        assertEquals("Wrong study", -7, (int) loaded.getStudy().getId());
    }

    public void testGetByAbbreviation() throws Exception {
        Population loaded = getDao().getByAbbreviation(studyDao.getById(-7), "M");
        assertEquals("Wrong population loaded", -76, (int) loaded.getId());
    }

    public void testGetByAbbreviationIsRestrictedByStudy() throws Exception {
        assertNull(getDao().getByAbbreviation(studyDao.getById(-8), "M"));
    }
    
    public void testGetAbbreviations() throws Exception {
        Set<String> actual = getDao().getAbbreviations(studyDao.getById(-7));
        assertEquals("Wrong number of abbreviations", 2, actual.size());
        assertContains("Missing abbreviation", actual, "H+");
        assertContains("Missing abbreviation", actual, "M");
    }
    
    public void testSave() throws Exception {
        Integer id;
        {
            Population newOne = new Population();
            newOne.setName("Novelty");
            newOne.setAbbreviation("N");
            newOne.setStudy(studyDao.getById(-7));
            getDao().save(newOne);
            id = newOne.getId();
            assertNotNull("ID not set", id);
        }

        interruptSession();

        Population reloaded = getDao().getById(id);
        assertNotNull("Could not reload", reloaded);
        assertEquals("Novelty", reloaded.getName());
        assertEquals("N", reloaded.getAbbreviation());
        assertEquals(-7, (int) reloaded.getStudy().getId());
    }

    public void testGetAllPopulations() throws Exception {
        Population p1 = getDao().getById(-77);
        Population p2 = getDao().getById(-88);
        Population p3 = getDao().getById(-76);
        List<Population> populations = getDao().getAll();
        assertEquals("Wrong number of populations ", 3, populations.size());
        assertEquals("First population in the list is not the one that expected ", populations.get(0).getName(), p1.getName() );
        assertEquals("Second population in the list is not the one that expected ", populations.get(1).getName(), p3.getName() );
        assertEquals("First population in the list is not the one that expected ", populations.get(2).getName(), p2.getName() );
    }
}
