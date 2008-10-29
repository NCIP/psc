package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.test.ConnectionSource;
import edu.northwestern.bioinformatics.studycalendar.test.RowPreservingInitializer;
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
 * @see UserService
 * @author Rhett Sutphin
 */
public class UsersInitializer extends RowPreservingInitializer implements InitializingBean {
    private UserDao userDao;
    private UserService userService;

    private Resource yaml;
    private Map<String, Map<String, List<String>>> userData; // parsed version of the YAML provided in yaml
    private SiteDao siteDao;


    public UsersInitializer() {
        super("user_role_sites", Arrays.asList("user_role_id", "site_id"));
    }

    @Transactional
    public void oneTimeSetup(ConnectionSource connectionSource) {
        super.oneTimeSetup(connectionSource);
        for (String username : userData.keySet()) {
            createOrUpdateUser(username, userData.get(username));
        }
    }

    private void createOrUpdateUser(String username, Map<String, List<String>> rolesAndSites) {
        User user = userDao.getByName(username);
        if (user == null) {
            user = new User();
            user.setName(username);
        } else {
            user.getUserRoles().clear();
        }
        for (String roleName : rolesAndSites.keySet()) {
            Role role = Role.valueOf(roleName);
            UserRole userRole = new UserRole(user, role);
            user.addUserRole(userRole);
            if (rolesAndSites.get(roleName) != null) {
                for (String siteAssignedIdentifier : rolesAndSites.get(roleName)) {
                    userRole.addSite(siteDao.getByAssignedIdentifier(siteAssignedIdentifier));
                }
            }
        }
        userService.saveUser(user, username, String.format("%s@psctest.example.net", username));
    }

    ////// CONFIGURATION

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setYamlResource(Resource data) {
        this.yaml = data;
    }

    @SuppressWarnings({ "unchecked" })
    public void afterPropertiesSet() throws Exception {
        userData = (Map<String, Map<String, List<String>>>) YAML.load(new InputStreamReader(yaml.getInputStream()));
    }
}
