package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Rhett Sutphin
 */
public class SuiteUserTest {
    private SuiteUser user;

    @Before
    public void before() throws Exception {
        user = new SuiteUser();
        user.setId(9);
        user.setUsername("jo");
        user.setFirstName("Josephine");
        user.setLastName("Miller");
        user.setEmailAddress("jo@m.example.net");
    }

    @Test
    public void validateComplainsAboutMissingId() throws Exception {
        user.setId(null);
        expectInvalidDetails(user, "Missing id");
    }

    @Test
    public void validateComplainsAboutMissingUsername() throws Exception {
        user.setUsername(null);
        expectInvalidDetails(user, "Missing username");
    }

    @Test
    public void validateComplainsAboutMissingFirstName() throws Exception {
        user.setFirstName(null);
        expectInvalidDetails(user, "Missing first name");
    }

    @Test
    public void validateComplainsAboutMissingLastName() throws Exception {
        user.setLastName(null);
        expectInvalidDetails(user, "Missing last name");
    }

    @Test
    public void validateComplainsAboutMissingEmailAddress() throws Exception {
        user.setEmailAddress(null);
        expectInvalidDetails(user, "Missing e-mail address");
    }

    @Test
    public void validateDoesNotComplainAboutNullEndDate() throws Exception {
        user.setAccountEndDate(null);
        expectValidDetails(user);
    }

    @Test
    public void validateCombinesMultipleInvalidities() throws Exception {
        user.setEmailAddress(null);
        user.setId(null);
        user.setLastName(null);

        expectInvalidDetails(user, "Missing id, last name, e-mail address");
    }

    @Test
    public void itHasAUsefulToString() throws Exception {
        assertThat(user.toString(), is("SuiteUser[jo]"));
    }

    private void expectValidDetails(SuiteUser actual) {
        actual.validateDetails(); // expect no error
    }

    private void expectInvalidDetails(SuiteUser actual, String expectedMessage) {
        try {
            actual.validateDetails();
            fail("Exception not thrown");
        } catch (InvalidSuiteUserException e) {
            assertThat(e.getMessage(), is(expectedMessage));
        }
    }
}
