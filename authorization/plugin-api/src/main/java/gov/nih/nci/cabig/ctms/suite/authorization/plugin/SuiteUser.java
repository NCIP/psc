/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An encapsulation of the user details and role memberships which an authorization plugin
 * may provide for each user.
 * <p>
 * Instances may be constructed using the traditional bean properties mechanism or using
 * a {@link Builder}. The latter is recommended because it will ensure that all mandatory fields
 * are set before handing back a user instance.
 *
 * @since 2.10
 * @author Rhett Sutphin
 */
public class SuiteUser {
    private Integer id;
    private String username, firstName, lastName, emailAddress;
    private Date accountEndDate;

    private Map<SuiteRole, SuiteRoleMembership> roleMemberships;

    private static final String[][] REQUIRED_PROPERTIES = {
        { "id", "id" },
        { "username", "username" },
        { "firstName", "first name" },
        { "lastName", "last name" },
        { "emailAddress", "e-mail address" }
    };

    public SuiteUser() {
        roleMemberships = new TreeMap<SuiteRole, SuiteRoleMembership>();
    }

    ////// LOGIC

    /**
     * Ensures that all the required fields are set. If any are missing, it throws
     * {@link InvalidSuiteUserException}.
     */
    public void validateDetails() throws InvalidSuiteUserException {
        List<String> missing = new ArrayList<String>(REQUIRED_PROPERTIES.length);

        BeanWrapper wrapped = new BeanWrapperImpl(this);
        for (String[] requiredProperty : REQUIRED_PROPERTIES) {
            String prop = requiredProperty[0], name = requiredProperty[1];
            if (wrapped.getPropertyValue(prop) == null) {
                missing.add(name);
            }
        }

        if (missing.size() > 0) {
            throw new InvalidSuiteUserException("Missing %s",
                StringUtils.collectionToDelimitedString(missing, ", "));
        }
    }

    ////// PROPERTIES

    /**
     * A unique numeric ID for the user. Required.
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The name the user uses to log in. Required.
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The user's first (personal) name. Required.
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * The user's last (family) name. Required.
     */
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * An e-mail address at which the user can be contacted. Required.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * The last day on which the user's account is valid. If null, the user's account will continue
     * to be valid indefinitely.
     */
    public Date getAccountEndDate() {
        return accountEndDate;
    }

    public void setAccountEndDate(Date accountEndDate) {
        this.accountEndDate = accountEndDate;
    }

    public Map<SuiteRole, SuiteRoleMembership> getRoleMemberships() {
        return roleMemberships;
    }

    public void setRoleMemberships(Map<SuiteRole, SuiteRoleMembership> roleMemberships) {
        this.roleMemberships = roleMemberships;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append('[').append(getUsername()).append(']').toString();
    }

    /**
     * A literate builder for {@link SuiteUser} instances. Performs validation to help
     * plugin implementations fail fast.
     */
    public static class Builder {
        private SuiteUser user;

        private boolean validateDetails;

        /**
         * Creates a builder with user detail validation enabled or disabled as specified.
         */
        public Builder(boolean validateDetails) {
            this.validateDetails = validateDetails;
            user = new SuiteUser();
        }

        /**
         * Creates a builder with user detail validation on.
         */
        public Builder() {
            this(true);
        }

        public Builder addRoleMembership(SuiteRoleMembership membership) {
            user.getRoleMemberships().put(membership.getRole(), membership);
            return this;
        }

        public Builder id(int id) {
            user.setId(id);
            return this;
        }

        public Builder username(String username) {
            user.setUsername(username);
            return this;
        }

        public Builder name(String first, String last) {
            user.setFirstName(first);
            user.setLastName(last);
            return this;
        }

        public Builder emailAddress(String addr) {
            user.setEmailAddress(addr);
            return this;
        }

        public Builder accountEndsOn(Date lastDay) {
            user.setAccountEndDate(lastDay);
            return this;
        }

        public SuiteUser toUser() throws InvalidSuiteUserException {
            if (validateDetails) user.validateDetails();
            return user;
        }
    }
}
