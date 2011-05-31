package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

/**
 * Defines the legal criteria for a search performed from {@link SuiteAuthorizationSource}.
 * String properties should be matched as substrings of the corresponding properties on
 * {@link SuiteUser}.
 *
 * @author Rhett Sutphin
 * @since 2.10
 */
public class SuiteUserSearchOptions {
    private String usernameSubstring, firstNameSubstring, lastNameSubstring;

    public String getUsernameSubstring() {
        return usernameSubstring;
    }

    public void setUsernameSubstring(String usernameSubstring) {
        this.usernameSubstring = usernameSubstring;
    }

    public String getFirstNameSubstring() {
        return firstNameSubstring;
    }

    public void setFirstNameSubstring(String firstNameSubstring) {
        this.firstNameSubstring = firstNameSubstring;
    }

    public String getLastNameSubstring() {
        return lastNameSubstring;
    }

    public void setLastNameSubstring(String lastNameSubstring) {
        this.lastNameSubstring = lastNameSubstring;
    }
}
