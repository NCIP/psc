package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.*;

/**
 * @author Rhett Sutphin
 */
public abstract class AbstractSingleUserProvisioningCommand {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String JSON_CHANGE_PROP_ROLE = "role";
    public static final String JSON_CHANGE_PROP_KIND = "kind";
    public static final String JSON_CHANGE_PROP_SCOPE_TYPE = "scopeType";
    public static final String JSON_CHANGE_PROP_SCOPE_IDENTIFIER = "scopeIdentifier";
    public static final String JSON_ALL_SCOPE_IDENTIFIER = "__ALL__";

    protected PscUser user;
    private JSONArray roleChanges;

    private ProvisioningSession provisioningSession;
    private final ProvisioningSessionFactory provisioningSessionFactory;
    private final ApplicationSecurityManager applicationSecurityManager;

    private List<ProvisioningRole> provisionableRoles;
    private boolean canProvisionAllSites;
    private List<Site> provisionableSites;
    private Set<String> provisionableSiteIdentifiers;
    private boolean canProvisionManagingAllStudies;
    private List<Study> provisionableManagedStudies;
    private Set<String> provisionableManagedStudyIdentifiers;
    private boolean canProvisionParticipateInAllStudies;
    private List<Study> provisionableParticipatingStudies;
    private Set<String> provisionableParticipatingStudyIdentifiers;
    protected static final int JSON_INDENT_DEPTH = 4;

    public AbstractSingleUserProvisioningCommand(
        PscUser user,
        ProvisioningSessionFactory provisioningSessionFactory,
        ApplicationSecurityManager applicationSecurityManager
    ) {
        this.user = user;
        this.applicationSecurityManager = applicationSecurityManager;
        this.provisioningSessionFactory = provisioningSessionFactory;

        // locked down by default
        this.provisionableRoles = Collections.emptyList();
        this.provisionableSites = Collections.emptyList();
        this.provisionableSiteIdentifiers = Collections.emptySet();
        this.provisionableManagedStudies = Collections.emptyList();
        this.provisionableManagedStudyIdentifiers = Collections.emptySet();
        this.provisionableParticipatingStudies = Collections.emptyList();
        this.provisionableParticipatingStudyIdentifiers = Collections.emptySet();
    }

    ////// APPLY

    public void apply() throws CSTransactionException {
        Map<String, List<SubmittedChange>> changesByType = classifyAndFilterChanges();
        applyAddAndRemoveScopes(changesByType.get("specificScope"));
        applyAddAndRemoveAllScope(changesByType.get("allScope"));
        applyAddAndRemoveGroupOnly(changesByType.get("groupOnly"));
        applyStaleFlag();
    }

    private void applyAddAndRemoveScopes(List<SubmittedChange> specificScopeChanges) {
        for (SubmittedChange change : specificScopeChanges) {
            SuiteRoleMembership base = getProvisioningSession().getProvisionableRoleMembership(change.getRole());
            if (change.isAdd()) {
                if (change.getScopeType() == ScopeType.SITE) base.addSite(change.getScopeIdentifier());
                else base.addStudy(change.getScopeIdentifier());
            } else if (change.isRemove()) {
                if (change.getScopeType() == ScopeType.SITE) base.removeSite(change.getScopeIdentifier());
                else base.removeStudy(change.getScopeIdentifier());
            }

            getProvisioningSession().replaceRole(base);
        }
    }

    private void applyAddAndRemoveAllScope(List<SubmittedChange> allScopeChanges) {
        for (SubmittedChange change : allScopeChanges) {
            SuiteRoleMembership base = getProvisioningSession().getProvisionableRoleMembership(change.getRole());
            if (change.isAdd()) {
                if (change.getScopeType() == ScopeType.SITE) base.forAllSites();
                else base.forAllStudies();
            } else if (change.isRemove()) {
                if (change.getScopeType() == ScopeType.SITE) base.notForAllSites();
                else base.notForAllStudies();
            }

            getProvisioningSession().replaceRole(base);
        }
    }

    private void applyAddAndRemoveGroupOnly(List<SubmittedChange> groupOnlyChanges) {
        for (SubmittedChange change : groupOnlyChanges) {
            if (change.isAdd()) {
                getProvisioningSession().replaceRole(
                    getProvisioningSession().getProvisionableRoleMembership(change.getRole()));
            } else if (change.isRemove()) {
                getProvisioningSession().deleteRole(change.getRole());
            }
        }
    }

    private void applyStaleFlag() {
        if (applicationSecurityManager == null) return;
        PscUser principal = applicationSecurityManager.getUser();
        if (principal.getCsmUser().getUserId().equals(getUser().getCsmUser().getUserId())) {
            principal.setStale(true);
        }
    }

    private ProvisioningSession getProvisioningSession() {
        if (provisioningSession == null) {
            provisioningSession =
                provisioningSessionFactory.createSession(getUser().getCsmUser().getUserId());
        }
        return provisioningSession;
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
        if (!provisionableRoles.contains(new ProvisioningRole(change.getRole()))) {
            log.warn("Ignoring unauthorized attempt to change {} membership.  Authorized to change only {}.",
                change.getRole(), provisionableRoles);
            return true;
        }
        boolean allPermission = false;
        Set<String> changeableIdents;
        if (change.getScopeType() == ScopeType.SITE) {
            allPermission = this.canProvisionAllSites;
            changeableIdents = this.provisionableSiteIdentifiers;
        } else {
            PscRole role = PscRole.valueOf(change.getRole());
            if (role == null) {
                // use participation for non-PSC roles
                allPermission = canProvisionParticipateInAllStudies;
                changeableIdents = provisionableParticipatingStudyIdentifiers;
            } else {
                changeableIdents = new HashSet<String>();
                if (role.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT)) {
                    changeableIdents.addAll(this.provisionableManagedStudyIdentifiers);
                    allPermission = this.canProvisionManagingAllStudies;
                }
                if (role.getUses().contains(PscRoleUse.SITE_PARTICIPATION)) {
                    changeableIdents.addAll(this.provisionableParticipatingStudyIdentifiers);
                    allPermission = allPermission || this.canProvisionParticipateInAllStudies;
                }
            }
        }

        if (change.isAllScope() && !allPermission) {
            log.warn("Ignoring unauthorized attempt to change all-{} access.",
                change.getScopeType().getPluralName());
            return true;
        }
        if (!change.isAllScope() && change.isScopeChange() && !changeableIdents.contains(change.getScopeIdentifier())) {
            log.warn("Ignoring unauthorized attempt to change {} \"{}\" access.  Authorized to change only {}.",
                new Object[] { change.getScopeType().getName(),
                    change.getScopeIdentifier(), changeableIdents });
            return true;
        }
        return false;
    }

    public Map<SuiteRole, SuiteRoleMembership> getCurrentRoles() {
        return user.getMemberships();
    }

    ////// JAVASCRIPT SERIALIZATION

    public String getJavaScriptProvisionableUser() {
        try {
            return String.format(
                "new psc.admin.ProvisionableUser('%s', %s)",
                getUser().getCsmUser().getLoginName(),
                buildProvisionableUserRoleJSON().toString(JSON_INDENT_DEPTH));
        } catch (JSONException e) {
            throw new StudyCalendarSystemException("Building JSON for provisionable user failed", e);
        }
    }

    private JSONObject buildProvisionableUserRoleJSON() throws JSONException {
        JSONObject rolesJSON = new JSONObject();
        for (Map.Entry<SuiteRole, SuiteRoleMembership> entry : getCurrentRoles().entrySet()) {
            rolesJSON.put(entry.getKey().getCsmName(), buildProvisionableUserScopeJSON(entry.getValue()));
        }
        return rolesJSON;
    }

    private JSONObject buildProvisionableUserScopeJSON(SuiteRoleMembership membership) throws JSONException {
        JSONObject scopeJSON = new JSONObject();
        for (ScopeType scopeType : ScopeType.values()) {
            if (membership.hasScope(scopeType)) {
                List<String> identifiers;
                if (membership.isAll(scopeType)) {
                    identifiers = Collections.singletonList(JSON_ALL_SCOPE_IDENTIFIER);
                } else {
                    identifiers = membership.getIdentifiers(scopeType);
                }
                scopeJSON.put(scopeType.getPluralName(), new JSONArray(identifiers));
            }
        }
        return scopeJSON;
    }

    ////// CONFIGURATION

    public List<ProvisioningRole> getProvisionableRoles() {
        return provisionableRoles;
    }

    public void setProvisionableRoles(SuiteRole... roles) {
        this.provisionableRoles = new ArrayList<ProvisioningRole>(roles.length);
        for (SuiteRole role : roles) {
            this.provisionableRoles.add(new ProvisioningRole(role));
        }
    }

    public void setProvisionableRoles(PscRole... roles) {
        SuiteRole[] sRoles = new SuiteRole[roles.length];
        for (int i = 0; i < roles.length; i++) {
            sRoles[i] = roles[i].getSuiteRole();
        }
        setProvisionableRoles(sRoles);
    }

    public List<Site> getProvisionableSites() {
        return provisionableSites;
    }

    public void setProvisionableSites(List<Site> provisionableSites) {
        this.provisionableSites = provisionableSites;
        this.provisionableSiteIdentifiers = new LinkedHashSet<String>();
        for (Site site : provisionableSites) {
            this.provisionableSiteIdentifiers.add(SITE_MAPPING.getSharedIdentity(site));
        }
    }

    public boolean getCanProvisionAllSites() {
        return canProvisionAllSites;
    }

    public void setCanProvisionAllSites(boolean canProvisionAllSites) {
        this.canProvisionAllSites = canProvisionAllSites;
    }

    public List<Study> getProvisionableManagedStudies() {
        return provisionableManagedStudies;
    }

    public void setProvisionableManagedStudies(List<Study> provisionableManagedStudies) {
        this.provisionableManagedStudies = provisionableManagedStudies;
        this.provisionableManagedStudyIdentifiers = new LinkedHashSet<String>();
        for (Study study : provisionableManagedStudies) {
            this.provisionableManagedStudyIdentifiers.add(STUDY_MAPPING.getSharedIdentity(study));
        }
    }

    public boolean getCanProvisionManagementOfAllStudies() {
        return canProvisionManagingAllStudies;
    }

    public void setCanProvisionManagingAllStudies(boolean canProvisionManagingAllStudies) {
        this.canProvisionManagingAllStudies = canProvisionManagingAllStudies;
    }

    public List<Study> getProvisionableParticipatingStudies() {
        return provisionableParticipatingStudies;
    }

    public void setProvisionableParticipatingStudies(List<Study> provisionableParticipatingStudies) {
        this.provisionableParticipatingStudies = provisionableParticipatingStudies;
        this.provisionableParticipatingStudyIdentifiers = new LinkedHashSet<String>();
        for (Study study : provisionableParticipatingStudies) {
            this.provisionableParticipatingStudyIdentifiers.add(STUDY_MAPPING.getSharedIdentity(study));
        }
    }

    public boolean getCanProvisionParticipationInAllStudies() {
        return canProvisionParticipateInAllStudies;
    }

    public void setCanProvisionParticipateInAllStudies(boolean canProvisionParticipateInAllStudies) {
        this.canProvisionParticipateInAllStudies = canProvisionParticipateInAllStudies;
    }

    ////// BOUND PROPERTIES

    public PscUser getUser() {
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

    ////// INNER CLASSES

    private class SubmittedChange {
        private boolean all;
        private String scopeIdentifier;
        private SuiteRole role;
        private ScopeType scopeType;
        private String kind;

        private SubmittedChange(JSONObject src) {
            this.kind = src.optString(JSON_CHANGE_PROP_KIND, null);
            this.role = SuiteRole.getByCsmName(src.optString(JSON_CHANGE_PROP_ROLE, null));
            String scopeTypeName = src.optString(JSON_CHANGE_PROP_SCOPE_TYPE, null);
            if (scopeTypeName != null) {
                this.all = JSON_ALL_SCOPE_IDENTIFIER.equals(src.optString(JSON_CHANGE_PROP_SCOPE_IDENTIFIER, null));
                this.scopeIdentifier = this.all ? null : src.optString(JSON_CHANGE_PROP_SCOPE_IDENTIFIER, null);
                scopeType = ScopeType.valueOf(scopeTypeName.toUpperCase());
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

        public ScopeType getScopeType() {
            return scopeType;
        }

        public boolean isAllScope() {
            return all;
        }

        public boolean isScopeChange() {
            return this.scopeType != null;
        }
    }

    public abstract static class ScopeComparator<T> implements Comparator<T> {
        public static final Comparator<String> IDENTITY = new ScopeComparator<String>() {
            @Override public String extractScopeIdentifier(String o) { return o; }
        };

        public int compare(T o1, T o2) {
            String id1 = extractScopeIdentifier(o1);
            String id2 = extractScopeIdentifier(o2);
            if (id1.equals(id2)) {
                return 0;
            } else if (JSON_ALL_SCOPE_IDENTIFIER.equals(id1)) {
                return -1;
            } else if (JSON_ALL_SCOPE_IDENTIFIER.equals(id2)) {
                return 1;
            } else {
                return id1.compareToIgnoreCase(id2);
            }
        }

        public abstract String extractScopeIdentifier(T o);
    }
}
