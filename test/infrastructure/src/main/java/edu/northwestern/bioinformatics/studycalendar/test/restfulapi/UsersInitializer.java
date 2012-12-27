/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.ConnectionSource;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.jvyaml.YAML;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Reads a set of users and roles in a YAML structure and creates or updates
 * corresponding users in the system.
 *
 * @author Rhett Sutphin
 */
public class UsersInitializer extends RowPreservingInitializer implements InitializingBean {
    private ProvisioningSessionFactory provisioningSessionFactory;
    private AuthorizationManager csmAuthorizationManager;

    private Resource yaml;
    // parsed version of the YAML provided in yaml
    private Map<String, Map<String, Map<String, Map<String, List<String>>>>> userData;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;


    public UsersInitializer() {
        super("csm_user", Arrays.asList("user_id"));
    }

    @Override
    @Transactional
    public void oneTimeSetup(ConnectionSource connectionSource) {
        super.oneTimeSetup(connectionSource);
        for (String username : userData.keySet()) {
            createOrUpdateUser(username, userData.get(username).get("roles"));
        }
    }

    private void createOrUpdateUser(String username, Map<String, Map<String, List<String>>> rolesAndScopes) {
        User user = csmAuthorizationManager.getUser(username);
        if (user == null) {
            User toCreate = AuthorizationObjectFactory.createCsmUser(username);
            configureUser(toCreate);
            try {
                csmAuthorizationManager.createUser(toCreate);
            } catch (CSTransactionException e) {
                throw new StudyCalendarSystemException("Creating user " + username + " failed");
            }
            user = csmAuthorizationManager.getUser(username);
        } else {
            configureUser(user);
            try {
                csmAuthorizationManager.modifyUser(user);
            } catch (CSTransactionException e) {
                throw new StudyCalendarSystemException("Updating user " + username + " failed");
            }
        }

        ProvisioningSession session = provisioningSessionFactory.createSession(user.getUserId());
        Map<SuiteRole, SuiteRoleMembership> memberships =
            suiteRoleMembershipLoader.getProvisioningRoleMemberships(user.getUserId());
        for (SuiteRole role : memberships.keySet()) {
            session.deleteRole(role);    
        }

        for (String roleName : rolesAndScopes.keySet()) {
            replaceRole(roleName, rolesAndScopes.get(roleName), session);
        }
    }

    // package level for testing
    User configureUser(User user) {
        String username = user.getLoginName();
        user.setFirstName(
            new StringBuilder(username).replace(0, 1, username.substring(0, 1).toUpperCase()).
                toString());
        user.setLastName("User");
        return user;
    }

    private void replaceRole(
        String roleName, Map<String, List<String>> scopes, ProvisioningSession session
    ) {
        SuiteRole role = SuiteRole.getByCsmName(roleName);
        SuiteRoleMembership membership = new SuiteRoleMembership(role, null, null);
        if (scopes != null) {
            for (String scopeName : scopes.keySet()) {
                applyScope(membership, scopeName, scopes.get(scopeName));
            }
        }
        session.replaceRole(membership);
    }

    private void applyScope(SuiteRoleMembership membership, String scopeName, List<String> scopeValue) {
        if ("sites".equals(scopeName)) {
            if (isAll(scopeValue)) {
                membership.forAllSites();
            } else {
                membership.forSites(scopeValue.toArray(new String[scopeValue.size()]));
            }
        } else if ("studies".equals(scopeName)) {
            if (isAll(scopeValue)) {
                membership.forAllStudies();
            } else {
                membership.forStudies(scopeValue.toArray(new String[scopeValue.size()]));
            }
        } else {
            throw new StudyCalendarSystemException("Unknown scope %s", scopeName);
        }
    }

    private boolean isAll(List<String> scopeValue) {
        return scopeValue.contains("__ALL__");
    }

    ////// CONFIGURATION

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }

    @Required
    public void setProvisioningSessionFactory(ProvisioningSessionFactory provisioningSessionFactory) {
        this.provisioningSessionFactory = provisioningSessionFactory;
    }

    @Required
    public void setSuiteRoleMembershipLoader(SuiteRoleMembershipLoader suiteRoleMembershipLoader) {
        this.suiteRoleMembershipLoader = suiteRoleMembershipLoader;
    }

    @Required
    public void setYamlResource(Resource data) {
        this.yaml = data;
    }

    @SuppressWarnings({ "unchecked" })
    public void afterPropertiesSet() throws Exception {
        userData = (Map<String, Map<String, Map<String, Map<String, List<String>>>>>)
            YAML.load(new InputStreamReader(yaml.getInputStream()));
    }
}
