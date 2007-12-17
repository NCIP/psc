package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;

/**
 * @author Rhett Sutphin
 */
public class PopulationDaoTest extends ContextDaoTestCase<PopulationDao> {
    public void testGetById() throws Exception {
        Population loaded = getDao().getById(-77);
        assertEquals("Hepatitis positive", loaded.getName());
        assertEquals("H+", loaded.getAbbreviation());
        assertEquals("Wrong study", -7, (int) loaded.getStudy().getId());
    }
}
