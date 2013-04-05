/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;
import edu.northwestern.bioinformatics.studycalendar.web.NewSubjectCommand;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;

import java.util.Date;

/**
 * @author Padmaja Vedula
 */
public class NewSubjectCommandTest extends TestCase {
    private NewSubjectCommand command = new NewSubjectCommand();

    public void testCreateSubject() throws Exception {
        String expectedLastName = "Scott";
        command.setFirstName("Tiger");
        command.setLastName(expectedLastName);
        command.setGender("Male");
        command.setDateOfBirth(new Date());
        command.setPersonId("123-45-5678");

        Subject subject = command.createSubject();
        assertEquals(expectedLastName, subject.getLastName());
        assertEquals("should not have any assigments", 0, subject.getAssignments().size());
    }
}
