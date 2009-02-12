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

    public void testApprovalsLoadedInOrderByApprovalDate() throws Exception {
        StudySite ss = getDao().getById(-301);
        assertEquals("Wrong number of approvals", 3, ss.getAmendmentApprovals().size());
        assertEquals("First approval should be earliest", -1036, (int) ss.getAmendmentApprovals().get(0).getId());
        assertEquals("Wrong second approval", -1050, (int) ss.getAmendmentApprovals().get(1).getId());
        assertEquals("Last approval should be latest", -1048, (int) ss.getAmendmentApprovals().get(2).getId());
    }
}
