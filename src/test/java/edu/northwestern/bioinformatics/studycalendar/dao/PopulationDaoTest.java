package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;

import java.util.Set;

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
}
