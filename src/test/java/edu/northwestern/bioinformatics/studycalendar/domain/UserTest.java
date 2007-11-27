package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class UserTest extends StudyCalendarTestCase {
    public void testFindByRole() throws Exception {
        User user = createNamedInstance("John", User.class);

        UserRole userRole0 = createUserRole(user, Role.SUBJECT_COORDINATOR);
        UserRole userRole1 = createUserRole(user, Role.SITE_COORDINATOR);
        user.addUserRole(userRole0);
        user.addUserRole(userRole1);

        assertSame("Wrong user role", userRole0, user.getUserRole(Role.SUBJECT_COORDINATOR));
        assertSame("Wrong user role", userRole1, user.getUserRole(Role.SITE_COORDINATOR));
    }
}
