package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.nwu.bioinformatics.commons.ComparisonUtils;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.Errors;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.SITE_MAPPING;

/**
 * @author Rhett Sutphin
 */
// TODO: this class should be reusable for provisioning flows other than the main user admin one,
// except that it needs to be slightly modified to allow for study scoping also.
public class ProvisionUserCommand implements Validatable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String CHANGE_PROP_ROLE = "role";
    private static final String CHANGE_PROP_KIND = "kind";
    private static final String CHANGE_PROP_SCOPE_TYPE = "scopeType";
    private static final String CHANGE_PROP_SCOPE_IDENTIFIER = "scopeIdentifier";
    private static final String ALL_SCOPE_IDENTIFIER = "__ALL__";

    private User user;
    private JSONArray roleChanges;
    private final Map<SuiteRole, SuiteRoleMembership> currentRoleMemberships;

    private ProvisioningSession provisioningSession;
    private final ProvisioningSessionFactory provisioningSessionFactory;
    private final AuthorizationManager authorizationManager;
    private final AuthenticationSystem authenticationSystem;

    private final List<SuiteRole> provisionableRoles;
    private final List<Site> provisionableSites;
    private final Set<String> provisionableSiteIdentifiers;
    private final boolean mayProvisionAllSites;
    private boolean lookUpBoundUser;
    private String password, rePassword;

    public ProvisionUserCommand(
        User user,
        Map<SuiteRole, SuiteRoleMembership> currentRoles,
        ProvisioningSessionFactory provisioningSessionFactory,
        AuthorizationManager authorizationManager,
        AuthenticationSystem authenticationSystem, List<SuiteRole> provisionableRoles,
        List<Site> provisionableSites, boolean mayProvisionAllSites
    ) {
        this.user = user;
        currentRoleMemberships = currentRoles;
        this.provisioningSessionFactory = provisioningSessionFactory;
        this.authorizationManager = authorizationManager;
        this.authenticationSystem = authenticationSystem;
        this.provisionableRoles = provisionableRoles;
        this.provisionableSites = provisionableSites;
        this.provisionableSiteIdentifiers = new LinkedHashSet<String>();
        for (Site site : provisionableSites) {
            this.provisionableSiteIdentifiers.add(SITE_MAPPING.getSharedIdentity(site));
        }
        this.mayProvisionAllSites = mayProvisionAllSites;
    }

    public static ProvisionUserCommand createForUnknownUser(
        ProvisioningSessionFactory psFactory, AuthorizationManager authorizationManager,
        AuthenticationSystem authenticationSystem, List<SuiteRole> suiteRoles, List<Site> sites, boolean mayProvisionAllSites
    ) {
        User user = new User();
        user.setUpdateDate(new Date()); // because CSM's toString NPEs without it
        return new ProvisionUserCommand(user,
            Collections.<SuiteRole, SuiteRoleMembership>emptyMap(),
            psFactory, authorizationManager,
            authenticationSystem, suiteRoles, sites, mayProvisionAllSites);
    }

    public void validate(Errors errors) {
        if (StringUtils.isBlank(user.getLoginName())) {
            errors.rejectValue("user.loginName", "error.user.name.not.specified");
        } else {
            User existing = authorizationManager.getUser(user.getLoginName());
            boolean existingMismatch = existing != null && !existing.getUserId().equals(user.getUserId());
            if (!lookUpBoundUser && ((isNewUser() && existing != null) || existingMismatch)) {
                errors.rejectValue("user.loginName", "error.user.name.already.exists");
            }
        }

        if (StringUtils.isBlank(user.getFirstName())) {
            errors.rejectValue("user.firstName", "error.user.firstName.not.specified");
        }
        if (StringUtils.isBlank(user.getLastName())) {
            errors.rejectValue("user.lastName", "error.user.lastName.not.specified");
        }

        if (!GenericValidator.isEmail(user.getEmailId())) {
            errors.rejectValue("user.emailId", "error.user.email.invalid");
        }

        if (isNewUser() && authenticationSystem.usesLocalPasswords() && (StringUtils.isBlank(user.getPassword()))) {
            errors.rejectValue("password", "error.user.password.not.specified");
        }
        if (getPassword() != null || getRePassword() != null) {
            if (!ComparisonUtils.nullSafeEquals(getPassword(), getRePassword())) {
                errors.rejectValue("rePassword", "error.user.repassword.does.not.match.password");
            }
        }
    }

    private boolean isNewUser() {
        return user.getUserId() == null;
    }

    public void apply() throws CSTransactionException {
        Map<String, List<SubmittedChange>> changesByType = classifyAndFilterChanges();
        applyPassword();
        saveOrUpdateUser();
        applyAddAndRemoveScopes(changesByType.get("specificScope"));
        applyAddAndRemoveAllScope(changesByType.get("allScope"));
        applyAddAndRemoveGroupOnly(changesByType.get("groupOnly"));
    }

    private void applyPassword() {
        if (authenticationSystem.usesLocalPasswords()) {
            if (!StringUtils.isBlank(getPassword())) {
                getUser().setPassword(getPassword());
            }
        } else {
            if (isNewUser()) {
                int length = 16 + (int) Math.round(16 * Math.random());
                StringBuilder generated = new StringBuilder();
                while (generated.length() < length) {
                    generated.append((char) (' ' + Math.round(('~' - ' ') * Math.random())));
                }
                getUser().setPassword(generated.toString());
            }
        }
    }

    private void saveOrUpdateUser() throws CSTransactionException {
        if (getUser().getUserId() == null && lookUpBoundUser) {
            User found = authorizationManager.getUser(getUser().getLoginName());
            if (found != null) {
                copyBoundProperties(this.user, found);
                this.user = found;
                authorizationManager.modifyUser(getUser());
            } else {
                authorizationManager.createUser(getUser());
            }
        } else if (getUser().getUserId() == null) {
            authorizationManager.createUser(getUser());
        } else {
            authorizationManager.modifyUser(getUser());
        }
    }

    private void copyBoundProperties(User src, User dst) {
        BeanWrapper srcW = new BeanWrapperImpl(src);
        BeanWrapper dstW = new BeanWrapperImpl(dst);

        for (PropertyDescriptor srcProp : srcW.getPropertyDescriptors()) {
            if (srcProp.getReadMethod() == null || srcProp.getWriteMethod() == null) {
                continue;
            }
            Object srcValue = srcW.getPropertyValue(srcProp.getName());
            if (srcValue != null) {
                dstW.setPropertyValue(srcProp.getName(), srcValue);
            }
        }
    }

    private void applyAddAndRemoveScopes(List<SubmittedChange> specificScopeChanges) {
        for (SubmittedChange change : specificScopeChanges) {
            SuiteRoleMembership base = getProvisioningSession().getProvisionableRoleMembership(change.getRole());
            if (change.isAdd()) {
                base.addSite(change.getScopeIdentifier());
            } else if (change.isRemove()) {
                base.removeSite(change.getScopeIdentifier());
            }

            ////// TODO: temporary
            if (base.getRole().isStudyScoped()) base.forAllStudies();

            getProvisioningSession().replaceRole(base);
        }
    }

    private void applyAddAndRemoveAllScope(List<SubmittedChange> allScopeChanges) {
        for (SubmittedChange change : allScopeChanges) {
            SuiteRoleMembership base = getProvisioningSession().getProvisionableRoleMembership(change.getRole());
            if (change.isAdd()) {
                base.forAllSites();
            } else if (change.isRemove()) {
                base.notForAllSites();
            }

            ////// TODO: temporary
            if (base.getRole().isStudyScoped()) base.forAllStudies();

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

    private ProvisioningSession getProvisioningSession() {
        if (provisioningSession == null) {
            provisioningSession = provisioningSessionFactory.createSession(getUser().getUserId());
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

    public String getJavaScriptProvisionableUser() {
        List<String> roleClauses = buildJsProvisionableUserRoleClauses();
        
        return String.format(
            "new psc.admin.ProvisionableUser('%s', {\n%s\n})",
            getUser().getLoginName(),
            StringUtils.join(roleClauses.iterator(), ",\n")
            );
    }

    private List<String> buildJsProvisionableUserRoleClauses() {
        List<String> roleClauses = new ArrayList<String>(getCurrentRoles().size());
        for (Map.Entry<SuiteRole, SuiteRoleMembership> entry : getCurrentRoles().entrySet()) {
            roleClauses.add(new StringBuilder().append("  ").append(entry.getKey().getCsmName()).
                append(": { ").
                append(StringUtils.join(buildJsProvisionableUserScopeClauses(entry.getValue()).iterator(), ", ")).
                append(" }").
                toString());
        }
        return roleClauses;
    }

    private List<String> buildJsProvisionableUserScopeClauses(SuiteRoleMembership membership) {
        List<String> clauses = new ArrayList<String>(ScopeType.values().length);
        for (ScopeType scopeType : ScopeType.values()) {
            if (membership.hasScope(scopeType)) {
                List<String> identifiers;
                if (membership.isAll(scopeType)) {
                    identifiers = Collections.singletonList(ALL_SCOPE_IDENTIFIER);
                } else {
                    identifiers = membership.getIdentifiers(scopeType);
                }
                clauses.add(String.format("%s: ['%s']",
                    scopeType.getPluralName(),
                    StringUtils.join(identifiers.iterator(), "', '")));
            }
        }
        return clauses;
    }

    ////// CONFIGURATION

    public List<SuiteRole> getProvisionableRoles() {
        return provisionableRoles;
    }

    public List<Site> getProvisionableSites() {
        return provisionableSites;
    }

    public Map<SuiteRole, SuiteRoleMembership> getCurrentRoles() {
        return currentRoleMemberships;
    }

    public void setLookUpBoundUser(boolean lookUpBoundUser) {
        this.lookUpBoundUser = lookUpBoundUser;
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

    /*
     * This array is parsed with the expectation that it will be the JSON-serialized result
     * of calling #roleChanges on the javascript object psc.admin.ProvisionableUser.
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    public void setRoleChanges(JSONArray roleChanges) {
        this.roleChanges = roleChanges;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRePassword() {
        return rePassword;
    }

    public void setRePassword(String rePassword) {
        this.rePassword = rePassword;
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
