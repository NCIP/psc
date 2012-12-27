/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AdministerUserCommand;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.security.AuthorizationManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class FirstAdministratorCommand extends AdministerUserCommand {
    public FirstAdministratorCommand(
        ProvisioningSessionFactory provisioningSessionFactory,
        AuthorizationManager authorizationManager,
        AuthenticationSystem authenticationSystem,
        CsmUserCache csmUserCache
    ) {
        super(null, provisioningSessionFactory, authorizationManager, authenticationSystem, null, csmUserCache);
        setLookUpBoundUser(true);
        setCanProvisionAllSites(true);
        setProvisionableRoles(SuiteRole.SYSTEM_ADMINISTRATOR, SuiteRole.USER_ADMINISTRATOR);
    }

    @Override
    public void apply() throws Exception {
        JSONObject addAdmin = new JSONObject();
        addAdmin.put(AdministerUserCommand.JSON_CHANGE_PROP_KIND, "add");
        addAdmin.put(
                AdministerUserCommand.JSON_CHANGE_PROP_ROLE, PscRole.SYSTEM_ADMINISTRATOR.getCsmName());

        JSONObject addUserAdmin = new JSONObject();
        addUserAdmin.put(AdministerUserCommand.JSON_CHANGE_PROP_KIND, "add");
        addUserAdmin.put(AdministerUserCommand.JSON_CHANGE_PROP_ROLE, PscRole.USER_ADMINISTRATOR.getCsmName());

        addUserAdmin.put(AdministerUserCommand.JSON_CHANGE_PROP_SCOPE_TYPE, ScopeType.SITE);
        addUserAdmin.put(JSON_CHANGE_PROP_SCOPE_IDENTIFIER, JSON_ALL_SCOPE_IDENTIFIER);

        getRoleChanges().put(getUser().getUsername(),
            new JSONArray(Arrays.asList(addAdmin, addUserAdmin)));

        super.apply();
    }
}
