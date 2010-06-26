package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
// TODO: this class should be reusable for provisioning flows other than the main user admin one,
// except that it needs to be slightly modified to allow for study scoping also.
public class UserAdministrationCommand {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String CHANGE_PROP_ROLE = "role";
    private static final String CHANGE_PROP_KIND = "kind";
    private static final String CHANGE_PROP_SCOPE_TYPE = "scopeType";
    private static final String CHANGE_PROP_SCOPE_IDENTIFIER = "scopeIdentifier";
    private static final String ALL_SCOPE_IDENTIFIER = "__ALL__";

    private User user;
    private JSONArray roleChanges;

    private final ProvisioningSession provisioningSession;
    private final AuthorizationManager authorizationManager;
    private final List<SuiteRole> provisionableRoles;
    private final List<Site> provisionableSites;
    private final Set<String> provisionableSiteIdentifiers;
    private final boolean mayProvisionAllSites;

    public UserAdministrationCommand(
        User user,
        ProvisioningSession provisioningSession, AuthorizationManager authorizationManager, 
        List<SuiteRole> provisionableRoles,
        List<Site> provisionableSites, boolean mayProvisionAllSites
    ) {
        this.user = user;
        this.provisioningSession = provisioningSession;
        this.authorizationManager = authorizationManager;
        this.provisionableRoles = provisionableRoles;
        this.provisionableSites = provisionableSites;
        this.provisionableSiteIdentifiers = new LinkedHashSet<String>();
        for (Site site : provisionableSites) {
            // TODO: replace getAssignedIdentifier with a call to the mapping
            this.provisionableSiteIdentifiers.add(site.getAssignedIdentifier());
        }
        this.mayProvisionAllSites = mayProvisionAllSites;
    }

    public void apply() throws CSTransactionException {
        Map<String, List<SubmittedChange>> changesByType = classifyAndFilterChanges();
        applyAddAndRemoveScopes(changesByType.get("specificScope"));
        applyAddAndRemoveAllScope(changesByType.get("allScope"));
        applyAddAndRemoveGroupOnly(changesByType.get("groupOnly"));

        authorizationManager.modifyUser(getUser());
    }

    private void applyAddAndRemoveScopes(List<SubmittedChange> specificScopeChanges) {
        for (SubmittedChange change : specificScopeChanges) {
            SuiteRoleMembership base = provisioningSession.getProvisionableRoleMembership(change.getRole());
            if (change.isAdd()) {
                base.addSite(change.getScopeIdentifier());
            } else if (change.isRemove()) {
                base.removeSite(change.getScopeIdentifier());
            }
            provisioningSession.replaceRole(base);
        }
    }

    private void applyAddAndRemoveAllScope(List<SubmittedChange> allScopeChanges) {
        for (SubmittedChange change : allScopeChanges) {
            SuiteRoleMembership base = provisioningSession.getProvisionableRoleMembership(change.getRole());
            if (change.isAdd()) {
                base.forAllSites();
            } else if (change.isRemove()) {
                // TODO: needs ctms-commons-suite-authorization 0.3.1+
                // base.notAllSites();
            }
            provisioningSession.replaceRole(base);
        }
    }

    private void applyAddAndRemoveGroupOnly(List<SubmittedChange> groupOnlyChanges) {
        for (SubmittedChange change : groupOnlyChanges) {
            if (change.isAdd()) {
                provisioningSession.replaceRole(
                    provisioningSession.getProvisionableRoleMembership(change.getRole())
                );
            } else if (change.isRemove()) {
                provisioningSession.deleteRole(change.getRole());
            }
        }
    }

    private Map<String, List<SubmittedChange>> classifyAndFilterChanges() {
        Map<String, List<SubmittedChange>> classified = new MapBuilder<String, List<SubmittedChange>>().
            put("specificScope", new LinkedList<SubmittedChange>()).
            put("allScope", new LinkedList<SubmittedChange>()).
            put("groupOnly", new LinkedList<SubmittedChange>()).
            toMap();
        for (int i = 0; i < getRoleChanges().length(); i++) {
            try {
                SubmittedChange change = new SubmittedChange(getRoleChanges().getJSONObject(i));
                if (shouldSkip(change)) continue;

                if (!change.isScopeChange()) {
                    classified.get("groupOnly").add(change);
                } else if (change.isAllScope()) {
                    classified.get("allScope").add(change);
                } else {
                    classified.get("specificScope").add(change);
                }
            } catch (JSONException e) {
                throw new StudyCalendarValidationException(
                    "One of the elements in the change list isn't an object or is otherwise invalid", e);
            }
        }
        return classified;
    }

    private boolean shouldSkip(SubmittedChange change) {
        if (!provisionableRoles.contains(change.getRole())) {
            log.warn("Ignoring unauthorized attempt to change {} membership.  Authorized to change only {}.",
                change.getRole(), provisionableRoles);
            return true;
        }
        if (change.isAllScope() && !this.mayProvisionAllSites) {
            log.warn("Ignoring unauthorized attempt to change all-sites access.");
            return true;
        }
        if (!change.isAllScope() && change.isScopeChange() && !provisionableSiteIdentifiers.contains(change.getScopeIdentifier())) {
            log.warn("Ignoring unauthorized attempt to change site \"{}\" access.  Authorized to change only {}.",
                change.getScopeIdentifier(), provisionableSiteIdentifiers);
            return true;
        }
        return false;
    }

    ////// ACCESSORS

    public List<SuiteRole> getProvisionableRoles() {
        return provisionableRoles;
    }

    public List<Site> getProvisionableSites() {
        return provisionableSites;
    }

    ////// BOUND PROPERTIES

    public User getUser() {
        return user;
    }

    public JSONArray getRoleChanges() {
        if (roleChanges == null) {
            roleChanges = new JSONArray();
        }
        return roleChanges;
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    public void setRoleChanges(JSONArray roleChanges) {
        this.roleChanges = roleChanges;
    }

    private class SubmittedChange {
        private boolean all;
        private String scopeIdentifier;
        private SuiteRole role;
        private String kind;
        private String scopeType;

        private SubmittedChange(JSONObject src) {
            this.kind = src.optString(CHANGE_PROP_KIND, null);
            this.role = SuiteRole.getByCsmName(src.optString(CHANGE_PROP_ROLE, null));
            this.scopeType = src.optString(CHANGE_PROP_SCOPE_TYPE, null);
            if (this.scopeType != null) {
                this.all = ALL_SCOPE_IDENTIFIER.equals(src.optString(CHANGE_PROP_SCOPE_IDENTIFIER, null));
                this.scopeIdentifier = this.all ? null : src.optString(CHANGE_PROP_SCOPE_IDENTIFIER, null);
            }
        }

        public boolean isAdd() {
            return "add".equals(kind);
        }

        public boolean isRemove() {
            return "remove".equals(kind);
        }

        public String getScopeIdentifier() {
            return scopeIdentifier;
        }

        public SuiteRole getRole() {
            return role;
        }

        public boolean isAllScope() {
            return all;
        }

        public boolean isScopeChange() {
            return this.scopeType != null;
        }
    }
}
