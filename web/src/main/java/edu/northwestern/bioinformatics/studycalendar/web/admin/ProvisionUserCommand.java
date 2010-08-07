package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleStudyParameters;
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
import java.util.Collection;
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
// TODO: this class should be reusable for provisioning flows other than the main user admin one,
// except that it needs to be slightly modified to allow for study scoping also.
public class ProvisionUserCommand implements Validatable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String JSON_CHANGE_PROP_ROLE = "role";
    public static final String JSON_CHANGE_PROP_KIND = "kind";
    public static final String JSON_CHANGE_PROP_SCOPE_TYPE = "scopeType";
    public static final String JSON_CHANGE_PROP_SCOPE_IDENTIFIER = "scopeIdentifier";
    public static final String JSON_ALL_SCOPE_IDENTIFIER = "__ALL__";
    private static final int JSON_INDENT_DEPTH = 4;

    private PscUser user;
    private JSONArray roleChanges;

    private ProvisioningSession provisioningSession;
    private final ProvisioningSessionFactory provisioningSessionFactory;
    private final AuthorizationManager authorizationManager;
    private final AuthenticationSystem authenticationSystem;
    private final ApplicationSecurityManager applicationSecurityManager;

    private List<ProvisioningRole> provisionableRoles;
    private List<Site> provisionableSites;
    private List<Study> provisionableManagedStudies, provisionableParticipatingStudies;
    private Set<String> provisionableSiteIdentifiers, 
        provisionableManagedStudyIdentifiers, provisionableParticipatingStudyIdentifiers;
    private boolean canProvisionAllSites,
        canProvisionManagingAllStudies, canProvisionParticipateInAllStudies;
    private boolean lookUpBoundUser;
    private String password, rePassword;

    private ProvisionUserCommand(
        PscUser user,
        ProvisioningSessionFactory provisioningSessionFactory,
        AuthorizationManager authorizationManager,
        AuthenticationSystem authenticationSystem,
        ApplicationSecurityManager applicationSecurityManager
    ) {
        this.user = user;
        this.provisioningSessionFactory = provisioningSessionFactory;
        this.authorizationManager = authorizationManager;
        this.authenticationSystem = authenticationSystem;
        this.applicationSecurityManager = applicationSecurityManager;

        // locked down by default
        this.provisionableRoles = Collections.emptyList();
        this.provisionableSites = Collections.emptyList();
        this.provisionableSiteIdentifiers = Collections.emptySet();
        this.provisionableManagedStudies = Collections.emptyList();
        this.provisionableManagedStudyIdentifiers = Collections.emptySet();
        this.provisionableParticipatingStudies = Collections.emptyList();
        this.provisionableParticipatingStudyIdentifiers = Collections.emptySet();
    }

    @SuppressWarnings({ "unchecked" })
    public static ProvisionUserCommand create(
        PscUser existingUser, ProvisioningSessionFactory psFactory,
        AuthorizationManager authorizationManager, AuthenticationSystem authenticationSystem,
        ApplicationSecurityManager applicationSecurityManager,
        SiteDao siteDao, StudyDao studyDao, PscUser provisioner
    ) {
        ProvisionUserCommand command = new ProvisionUserCommand(
            existingUser == null ? AuthorizationObjectFactory.createPscUser() : existingUser,
            psFactory, authorizationManager, authenticationSystem, applicationSecurityManager);
        if (provisioner == null) return command;

        if (provisioner.getMembership(PscRole.USER_ADMINISTRATOR) != null) {
            SuiteRoleMembership ua = provisioner.getMembership(PscRole.USER_ADMINISTRATOR);
            command.setProvisionableRoles(SuiteRole.values());
            command.setProvisionableSites(ua.isAllSites() ? siteDao.getAll() : (List<Site>) ua.getSites());
            command.setCanProvisionAllSites(ua.isAllSites());
            VisibleStudyParameters provisionable = new VisibleStudyParameters();
            if (ua.isAllSites()) {
                provisionable.forAllManagingSites().forAllParticipatingSites();
            } else {
                provisionable.forManagingSiteIdentifiers(ua.getSiteIdentifiers()).
                    forParticipatingSiteIdentifiers(ua.getSiteIdentifiers());
            }
            command.setProvisionableManagedStudies(
                studyDao.getVisibleStudiesForTemplateManagement(provisionable));
            command.setProvisionableParticipatingStudies(
                studyDao.getVisibleStudiesForSiteParticipation(provisionable));
            command.setCanProvisionManagingAllStudies(true);
            command.setCanProvisionParticipateInAllStudies(true);
        } else if (provisioner.getMembership(PscRole.SYSTEM_ADMINISTRATOR) != null) {
            command.setProvisionableRoles(SuiteRole.USER_ADMINISTRATOR, SuiteRole.SYSTEM_ADMINISTRATOR);
            command.setProvisionableSites(Collections.<Site>emptyList());
            command.setCanProvisionAllSites(true);
        }

        return command;
    }

    public void validate(Errors errors) {
        if (StringUtils.isBlank(getUser().getLoginName())) {
            errors.rejectValue("user.loginName", "error.user.name.not.specified");
        } else {
            User existing = authorizationManager.getUser(getUser().getLoginName());
            boolean existingMismatch = existing != null && !existing.getUserId().equals(getUser().getUserId());
            if (!lookUpBoundUser && ((isNewUser() && existing != null) || existingMismatch)) {
                errors.rejectValue("user.loginName", "error.user.name.already.exists");
            }
        }

        if (StringUtils.isBlank(getUser().getFirstName())) {
            errors.rejectValue("user.firstName", "error.user.firstName.not.specified");
        }
        if (StringUtils.isBlank(getUser().getLastName())) {
            errors.rejectValue("user.lastName", "error.user.lastName.not.specified");
        }

        if (!GenericValidator.isEmail(getUser().getEmailId())) {
            errors.rejectValue("user.emailId", "error.user.email.invalid");
        }

        if (isNewUser() && getUsesLocalPasswords() && (StringUtils.isBlank(getPassword()))) {
            errors.rejectValue("password", "error.user.password.not.specified");
        }
        if (getPassword() != null || getRePassword() != null) {
            if (!ComparisonUtils.nullSafeEquals(getPassword(), getRePassword())) {
                errors.rejectValue("rePassword", "error.user.repassword.does.not.match.password");
            }
        }
    }

    public boolean isNewUser() {
        return getUser().getUserId() == null;
    }

    public void apply() throws CSTransactionException {
        Map<String, List<SubmittedChange>> changesByType = classifyAndFilterChanges();
        applyPassword();
        saveOrUpdateUser();
        applyAddAndRemoveScopes(changesByType.get("specificScope"));
        applyAddAndRemoveAllScope(changesByType.get("allScope"));
        applyAddAndRemoveGroupOnly(changesByType.get("groupOnly"));
        applyStaleFlag();
    }

    private void applyPassword() {
        if (getUsesLocalPasswords()) {
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
                copyBoundProperties(this.getUser(), found);
                this.user = AuthorizationObjectFactory.createPscUser(found);
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
        if (principal.getCsmUser().getUserId().equals(getUser().getUserId())) {
            principal.setStale(true);
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

    public String getJavaScriptProvisionableUser() {
        try {
            return String.format(
                "new psc.admin.ProvisionableUser('%s', %s)",
                getUser().getLoginName(),
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

    public String getJavaScriptProvisionableSites() {
        try {
            return buildJavaScriptProvisionableSites().toString(JSON_INDENT_DEPTH);
        } catch (JSONException e) {
            throw new StudyCalendarSystemException(
                "Building JSON for provisionable sites failed", e);
        }
    }

    JSONArray buildJavaScriptProvisionableSites() {
        JSONArray sites = new JSONArray();
        if (getCanProvisionAllSites()) {
            sites.put(new MapBuilder<String, String>().
                put("identifier", JSON_ALL_SCOPE_IDENTIFIER).
                put("name", allName(ScopeType.SITE)).
                toMap());
        }
        for (Site site : provisionableSites) {
            sites.put(new MapBuilder<String, String>().
                put("name", site.getName()).
                put("identifier", site.getAssignedIdentifier()).
                toMap());
        }
        return sites;
    }

    public String getJavaScriptProvisionableRoles() {
        try {
            JSONArray roles = new JSONArray();
            for (ProvisioningRole role : provisionableRoles) {
                roles.put(role.toJSON());
            }
            return roles.toString(JSON_INDENT_DEPTH);
        } catch (JSONException e) {
            throw new StudyCalendarSystemException(
                "Building JSON for provisionable roles failed", e);
        }
    }

    public String getJavaScriptProvisionableStudies() {
        try {
            return buildJavaScriptProvisionableStudies().toString(JSON_INDENT_DEPTH);
        } catch (JSONException e) {
            throw new StudyCalendarSystemException(
                "Building JSON for provisionable studies failed", e);
        }
    }

    // package level for testing
    JSONObject buildJavaScriptProvisionableStudies() throws JSONException {
        JSONObject studies = new JSONObject();
        buildJavaScriptStudyList(studies, PscRoleUse.TEMPLATE_MANAGEMENT.name().toLowerCase(),
            getCanProvisionManagementOfAllStudies(), getProvisionableManagedStudies());
        buildJavaScriptStudyList(studies, PscRoleUse.SITE_PARTICIPATION.name().toLowerCase(),
            getCanProvisionParticipationInAllStudies(), getProvisionableParticipatingStudies());
        Set<Study> allStudies = new HashSet<Study>();
        allStudies.addAll(getProvisionableParticipatingStudies());
        allStudies.addAll(getProvisionableManagedStudies());
        buildJavaScriptStudyList(studies,
            PscRoleUse.TEMPLATE_MANAGEMENT.name().toLowerCase() + '+' +
                PscRoleUse.SITE_PARTICIPATION.name().toLowerCase(),
            getCanProvisionParticipationInAllStudies() || getCanProvisionManagementOfAllStudies(),
            allStudies);
        return studies;
    }

    private void buildJavaScriptStudyList(
        JSONObject studies, String key, boolean canProvisionAll, Collection<Study> provisionableStudies
    ) throws JSONException {
        List<JSONObject> a = new ArrayList<JSONObject>(1 + provisionableStudies.size());
        if (canProvisionAll) {
            a.add(new JSONObject(new MapBuilder<String, String>().
                put("identifier", JSON_ALL_SCOPE_IDENTIFIER).
                put("name", allName(ScopeType.STUDY)).
                toMap()));
        }
        for (Study study : provisionableStudies) {
            a.add(new JSONObject(new MapBuilder<String, String>().
                put("identifier", study.getAssignedIdentifier()).
                put("name", study.getName()).
                toMap()));
        }
        Collections.sort(a, StudyJSONObjectComparator.INSTANCE);
        studies.put(key, a);
    }

    private String allName(ScopeType scopeType) {
        return String.format(
            "All %s (this user will have access in this role for all %s, including new ones as they are created)",
            scopeType.getPluralName(), scopeType.getPluralName());
    }

    ////// CONFIGURATION

    public Map<SuiteRole, SuiteRoleMembership> getCurrentRoles() {
        return user.getMemberships();
    }

    public List<ProvisioningRole> getProvisionableRoles() {
        return provisionableRoles;
    }

    public void setProvisionableRoles(SuiteRole... roles) {
        this.provisionableRoles = new ArrayList<ProvisioningRole>(roles.length);
        for (SuiteRole role : roles) {
            this.provisionableRoles.add(new ProvisioningRole(role));
        }
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

    public void setLookUpBoundUser(boolean lookUpBoundUser) {
        this.lookUpBoundUser = lookUpBoundUser;
    }

    public boolean getUsesLocalPasswords() {
        return authenticationSystem.usesLocalPasswords();
    }

    ////// BOUND PROPERTIES

    public User getUser() {
        // all the bindable properties are on the CSM user object, so this is simpler
        return user.getCsmUser();
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

    private static class StudyJSONObjectComparator implements Comparator<JSONObject> {
        public static final Comparator<? super JSONObject> INSTANCE =
            new StudyJSONObjectComparator();

        public int compare(JSONObject o1, JSONObject o2) {
            String id1 = o1.optString("identifier");
            String id2 = o2.optString("identifier");
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

        private StudyJSONObjectComparator() { }
    }
}
