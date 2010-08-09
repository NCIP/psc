package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AdministerUserCommand;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.security.AuthorizationManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class FirstAdministratorCommand extends AdministerUserCommand {
    public FirstAdministratorCommand(
        ProvisioningSessionFactory provisioningSessionFactory,
        AuthorizationManager authorizationManager,
        AuthenticationSystem authenticationSystem
    ) {
        super(null, provisioningSessionFactory, authorizationManager, authenticationSystem, null);
        setLookUpBoundUser(true);
        setProvisionableRoles(SuiteRole.SYSTEM_ADMINISTRATOR);
    }

    @Override
    public void apply() throws Exception {
        JSONObject addSysAdmin = new JSONObject();
        addSysAdmin.put(AdministerUserCommand.JSON_CHANGE_PROP_KIND, "add");
        addSysAdmin.put(
            AdministerUserCommand.JSON_CHANGE_PROP_ROLE, PscRole.SYSTEM_ADMINISTRATOR.getCsmName());
        getRoleChanges().put(getUser().getUsername(),
            new JSONArray(Collections.singletonList(addSysAdmin)));

        super.apply();
    }
}
