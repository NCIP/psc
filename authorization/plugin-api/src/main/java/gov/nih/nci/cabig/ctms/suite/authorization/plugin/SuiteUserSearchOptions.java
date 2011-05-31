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

    ////// FACTORIES

    /**
     * @return options that all users should be returned.
     */
    public static SuiteUserSearchOptions allUsers() {
        return new SuiteUserSearchOptions();
    }

    /**
     * @return options indicating a search on username only.
     */
    public static SuiteUserSearchOptions forUsernameSubstring(String namePart) {
        SuiteUserSearchOptions opts = new SuiteUserSearchOptions();
        opts.setUsernameSubstring(namePart);
        return opts;
    }

    /**
     * @return options indicating a search on first name only.
     */
    public static SuiteUserSearchOptions forFirstNameSubstring(String namePart) {
        SuiteUserSearchOptions opts = new SuiteUserSearchOptions();
        opts.setFirstNameSubstring(namePart);
        return opts;
    }

    /**
     * @return options indicating a search on last name only.
     */
    public static SuiteUserSearchOptions forLastNameSubstring(String namePart) {
        SuiteUserSearchOptions opts = new SuiteUserSearchOptions();
        opts.setLastNameSubstring(namePart);
        return opts;
    }

    /**
     * @return options indicating a search on username, first name, and last name.
     */
    public static SuiteUserSearchOptions forAllNames(String namePart) {
        SuiteUserSearchOptions opts = new SuiteUserSearchOptions();
        opts.setUsernameSubstring(namePart);
        opts.setFirstNameSubstring(namePart);
        opts.setLastNameSubstring(namePart);
        return opts;
    }

    ////// PROPERTIES

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

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuiteUserSearchOptions that = (SuiteUserSearchOptions) o;

        if (firstNameSubstring != null ? !firstNameSubstring.equals(that.firstNameSubstring) : that.firstNameSubstring != null)
            return false;
        if (lastNameSubstring != null ? !lastNameSubstring.equals(that.lastNameSubstring) : that.lastNameSubstring != null)
            return false;
        if (usernameSubstring != null ? !usernameSubstring.equals(that.usernameSubstring) : that.usernameSubstring != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = usernameSubstring != null ? usernameSubstring.hashCode() : 0;
        result = 31 * result + (firstNameSubstring != null ? firstNameSubstring.hashCode() : 0);
        result = 31 * result + (lastNameSubstring != null ? lastNameSubstring.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[usernameSubstring=").append(getUsernameSubstring()).
            append("; firstNameSubstring=").append(getFirstNameSubstring()).
            append("; lastNameSubstring=").append(getLastNameSubstring()).
            append(']').toString();
    }
}
