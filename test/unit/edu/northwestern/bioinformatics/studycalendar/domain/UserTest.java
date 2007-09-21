package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

public class UserTest extends StudyCalendarTestCase {
    public void testUserPassword() throws Exception {
        User user = new User();
        user.setPlainTextPassword("password123");
        assertEquals("Wrong retrieved password: ", "password123", user.getPlainTextPassword());
    }
}
