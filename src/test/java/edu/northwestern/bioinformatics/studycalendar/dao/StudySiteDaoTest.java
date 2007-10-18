package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

public class StudySiteDaoTest  extends ContextDaoTestCase<StudySiteDao> {
    public void testGetById() throws Exception {
        StudySite studySite = getDao().getById(-300);

        assertEquals("Wrong Study", "Study A", studySite.getStudy().getName());
        assertEquals("Wrong User" , "Joey"   , studySite.getUsers().get(0).getName());
    }
}
