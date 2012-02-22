package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Rhett Sutphin
 */
public class SuiteUserBuilderTest {
    @Test
    public void addRoleMembership() throws Exception {
        SuiteRoleMembership expected = new SuiteRoleMembership(SuiteRole.DATA_IMPORTER, null, null);

        SuiteUser built = builder().
            addRoleMembership(expected).
            toUser();

        assertThat(
            built.getRoleMemberships().get(SuiteRole.DATA_IMPORTER), is(sameInstance(expected)));
    }

    @Test
    public void setId() throws Exception {
        assertThat(builder().id(5).toUser().getId(), is(5));
    }

    @Test
    public void setUsername() throws Exception {
        assertThat(builder().username("jo").toUser().getUsername(), is("jo"));
    }

    @Test
    public void setFirstName() throws Exception {
        assertThat(builder().name("Josephine", "Mueller").toUser().getFirstName(), is("Josephine"));
    }

    @Test
    public void setLastName() throws Exception {
        assertThat(builder().name("Josephine", "Mueller").toUser().getLastName(), is("Mueller"));
    }

    @Test
    public void setEmailAddress() throws Exception {
        assertThat(builder().emailAddress("jo@example.edu").toUser().getEmailAddress(),
            is("jo@example.edu"));
    }

    @Test
    public void setEndDate() throws Exception {
        assertThat(builder().accountEndsOn(new Date()).toUser().getAccountEndDate(),
            is(not(nullValue())));
    }

    @Test
    public void toUserValidatesByDefault() throws Exception {
        try {
            new SuiteUser.Builder().
                id(4).username("fred").emailAddress("freddo@o.example.com").
                toUser();
            fail("Exception not thrown");
        } catch (InvalidSuiteUserException e) {
            assertThat(e.getMessage(), is("Missing first name, last name"));
        }
    }

    private SuiteUser.Builder builder() {
        return new SuiteUser.Builder(false);
    }
}
