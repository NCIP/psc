/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAssignment;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class StudySubjectAssignmentPrivilegeTest extends WebTestCase {
    public void testValuesFor() throws Exception {
        UserStudySubjectAssignmentRelationship ussar = registerNiceMockFor(UserStudySubjectAssignmentRelationship.class);
        expect(ussar.isVisible()).andReturn(true);
        expect(ussar.getCanUpdateSchedule()).andReturn(true);
        expect(ussar.isCalendarManager()).andReturn(true);

        replayMocks();
        List<StudySubjectAssignmentPrivilege> actual = StudySubjectAssignmentPrivilege.valuesFor(ussar);
        verifyMocks();

        assertEquals("Wrong number of privileges", 3, actual.size());
        assertEquals("Wrong first priv", StudySubjectAssignmentPrivilege.VISIBLE, actual.get(0));
        assertEquals("Wrong second priv", StudySubjectAssignmentPrivilege.UPDATE_SCHEDULE, actual.get(1));
        assertEquals("Wrong third priv", StudySubjectAssignmentPrivilege.CALENDAR_MANAGER, actual.get(2));
    }

    public void testAllPrivilegePropertiesExist() throws Exception {
        PscUser user = AuthorizationObjectFactory.createPscUser("jo",
                createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies().forAllSites(),
                createSuiteRoleMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllStudies().forAllSites(),
                createSuiteRoleMembership(PscRole.DATA_READER).forAllStudies().forAllSites());
        StudySubjectAssignment assignment =  createAssignment(new Study(),new Site(), new Subject());
        UserStudySubjectAssignmentRelationship ussar = new UserStudySubjectAssignmentRelationship(user, assignment);
        BeanWrapper bw = new BeanWrapperImpl(ussar);

        for (StudySubjectAssignmentPrivilege privilege : StudySubjectAssignmentPrivilege.values()) {
            assertTrue("No property named " + privilege.getPropertyName() + " for " + privilege,
                bw.isReadableProperty(privilege.getPropertyName()));
        }
    }

    public void testLookUp() throws Exception {
        StudySubjectAssignmentPrivilege actual = StudySubjectAssignmentPrivilege.lookUp("visible");
        assertEquals("Wrong Enum", StudySubjectAssignmentPrivilege.VISIBLE, actual);
    }

    public void testLookUpForIllegalEnumName() throws Exception {
        StudySubjectAssignmentPrivilege actual = StudySubjectAssignmentPrivilege.lookUp("not-visible");
        assertNull("Enum created", actual);
    }
}

