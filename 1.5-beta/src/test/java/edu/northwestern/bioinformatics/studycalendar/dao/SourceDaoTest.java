package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import java.util.List;

public class SourceDaoTest extends DaoTestCase {
    private SourceDao dao = (SourceDao) getApplicationContext().getBean("sourceDao");

    public void testGetById() throws Exception {
        Source source = dao.getById(-1);
        assertEquals("Wrong source name", "ICD-9", source.getName());
        assertEquals("Wrong number of activities", 2, source.getActivities().size());
        assertEquals("Wrong activity name", "Screening Activity", source.getActivities().get(0).getName());
        assertEquals("Wrong activity name", "Administer Drug Z" , source.getActivities().get(1).getName());
    }

    public void testGetAll() throws Exception {
        List<Source> sources = dao.getAll();
        assertEquals("Wrong size", 2, sources.size());
        assertEquals("Wrong name", "Empty", sources.get(0).getName());
        assertEquals("Wrong name", "ICD-9", sources.get(1).getName());
    }
}
