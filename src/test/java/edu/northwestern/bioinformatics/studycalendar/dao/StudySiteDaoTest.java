package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

public class StudySiteDaoTest  extends ContextDaoTestCase<StudySiteDao> {
    public void testGetById() throws Exception {
        StudySite studySite = getDao().getById(-300);

        assertEquals("Wrong Study", "Study A", studySite.getStudy().getName());
        assertEquals("Wrong User" , "Joey"   , studySite.getUserRoles().get(0).getUser().getName());
        
        assertEquals("Wrong number of approvals", 1, studySite.getAmendmentApprovals().size());
        assertEquals("Wrong approval", -310, (int) studySite.getAmendmentApprovals().get(0).getId());
    }
}
