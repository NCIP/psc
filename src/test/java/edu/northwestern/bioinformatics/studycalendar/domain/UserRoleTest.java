package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.Set;

public class UserRoleTest extends StudyCalendarTestCase {

    public void testFindByRole() throws Exception {
        User user = createNamedInstance("John", User.class);

        UserRole userRole0 = createUserRole(user, Role.PARTICIPANT_COORDINATOR);
        UserRole userRole1 = createUserRole(user, Role.SITE_COORDINATOR);

        Set<UserRole> userRoles = new HashSet(asList(userRole0, userRole1));

        UserRole actualUserRole = findByRole(userRoles, Role.PARTICIPANT_COORDINATOR);
        assertTrue("Wrong User Role", userRole0 == actualUserRole);
    }
}
