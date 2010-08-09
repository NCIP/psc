package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.security.authorization.domainobjects.User;

/**
 * This is testing both that
 * {@link edu.northwestern.bioinformatics.studycalendar.dao.AssignmentManagerResolverListener}
 * works and that it is configured into the application's session factory.
 *
 * @author Rhett Sutphin
 */
public class AssignmentManagerResolverListenerIntegratedTest extends DaoTestCase {
    private StudySubjectAssignmentDao dao;

    @Override
    protected String getTestDataFileName() {
        return String.format("testdata/%s.xml",
            StudySubjectAssignmentDaoTest.class.getSimpleName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dao = (StudySubjectAssignmentDao) getApplicationContext().getBean("studySubjectAssignmentDao");
    }

    public void testManagerUserLoadedAutomatically() throws Exception {
        User actual = dao.getById(-10).getStudySubjectCalendarManager();
        assertNotNull("User not loaded", actual);
        assertEquals("Wrong user", "jo", actual.getLoginName());
    }

    public void testUnknownManagerUserDoesNotCauseAProblem() throws Exception {
        StudySubjectAssignment assignment = dao.getById(-12);
        // expect no error during load
        assertEquals((Integer) (-8901), assignment.getManagerCsmUserId());
    }
}
