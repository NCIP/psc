package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Rhett Sutphin
 */
public class SuiteUserSearchOptionsTest {
    @Test
    public void forUsernameFactorySetsUsernameOnly() throws Exception {
        SuiteUserSearchOptions actual = SuiteUserSearchOptions.forUsernameSubstring("bippy");
        assertUserSearchOptions(actual, "bippy", null, null);
    }

    @Test
    public void forFirstNameFactorySetsFirstNameOnly() throws Exception {
        SuiteUserSearchOptions actual = SuiteUserSearchOptions.forFirstNameSubstring("bippy");
        assertUserSearchOptions(actual, null, "bippy", null);
    }

    @Test
    public void forLastNameFactorySetsLastNameOnly() throws Exception {
        SuiteUserSearchOptions actual = SuiteUserSearchOptions.forLastNameSubstring("bippy");
        assertUserSearchOptions(actual, null, null, "bippy");
    }

    @Test
    public void forAllNamesFactorySetsAllCriteria() throws Exception {
        SuiteUserSearchOptions actual = SuiteUserSearchOptions.forAllNames("bippy");
        assertUserSearchOptions(actual, "bippy", "bippy", "bippy");
    }

    private void assertUserSearchOptions(
        SuiteUserSearchOptions actual,
        String expectedUsername, String expectedFirst, String expectedLast
    ) {
        assertEquals("Wrong username", expectedUsername, actual.getUsernameSubstring());
        assertEquals("Wrong first name", expectedFirst, actual.getFirstNameSubstring());
        assertEquals("Wrong last name", expectedLast, actual.getLastNameSubstring());
    }
}
