package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.Errors;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The command for creating or updating a single user.
 *
 * @author Rhett Sutphin
 */
public class AdministerUserCommand
    extends BaseUserProvisioningCommand
    implements Validatable
{
    private final AuthorizationManager authorizationManager;
    private final AuthenticationSystem authenticationSystem;

    private boolean lookUpBoundUser;
    private String password, rePassword;

    protected AdministerUserCommand(
        PscUser user,
        ProvisioningSessionFactory provisioningSessionFactory,
        AuthorizationManager authorizationManager,
        AuthenticationSystem authenticationSystem,
        ApplicationSecurityManager applicationSecurityManager
    ) {
        super(user == null ? AuthorizationObjectFactory.createPscUser() : user,
            provisioningSessionFactory, applicationSecurityManager);
        this.authorizationManager = authorizationManager;
        this.authenticationSystem = authenticationSystem;
    }

    @SuppressWarnings({ "unchecked" })
    public static AdministerUserCommand create(
        PscUser existingUser, ProvisioningSessionFactory psFactory,
        AuthorizationManager authorizationManager, AuthenticationSystem authenticationSystem,
        ApplicationSecurityManager applicationSecurityManager,
        SiteDao siteDao, StudyDao studyDao, PscUser provisioner
    ) {
        AdministerUserCommand command = new AdministerUserCommand(existingUser,
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
        if (StringUtils.isBlank(getUser().getCsmUser().getLoginName())) {
            errors.rejectValue("user.loginName", "error.user.name.not.specified");
        } else {
            User existing = authorizationManager.getUser(getUser().getCsmUser().getLoginName());
            boolean existingMismatch = existing != null && !existing.getUserId().equals(getUser().getCsmUser().getUserId());
            if (!lookUpBoundUser && ((isNewUser() && existing != null) || existingMismatch)) {
                errors.rejectValue("user.loginName", "error.user.name.already.exists");
            }
        }

        if (StringUtils.isBlank(getUser().getCsmUser().getFirstName())) {
            errors.rejectValue("user.firstName", "error.user.firstName.not.specified");
        }
        if (StringUtils.isBlank(getUser().getCsmUser().getLastName())) {
            errors.rejectValue("user.lastName", "error.user.lastName.not.specified");
        }

        if (!GenericValidator.isEmail(getUser().getCsmUser().getEmailId())) {
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
        return getUser().getCsmUser().getUserId() == null;
    }

    @Override
    public void apply() throws Exception {
        applyPassword();
        saveOrUpdateUser();
        super.apply();
    }

    private void applyPassword() {
        if (getUsesLocalPasswords()) {
            if (!StringUtils.isBlank(getPassword())) {
                getUser().getCsmUser().setPassword(getPassword());
            }
        } else {
            if (isNewUser()) {
                int length = 16 + (int) Math.round(16 * Math.random());
                StringBuilder generated = new StringBuilder();
                while (generated.length() < length) {
                    generated.append((char) (' ' + Math.round(('~' - ' ') * Math.random())));
                }
                getUser().getCsmUser().setPassword(generated.toString());
            }
        }
    }

    private void saveOrUpdateUser() throws CSTransactionException {
        if (isNewUser() && lookUpBoundUser) {
            User found = authorizationManager.getUser(getUser().getCsmUser().getLoginName());
            if (found != null) {
                copyBoundProperties(this.getUser().getCsmUser(), found);
                setUser(AuthorizationObjectFactory.createPscUser(found));
                authorizationManager.modifyUser(getUser().getCsmUser());
            } else {
                authorizationManager.createUser(getUser().getCsmUser());
            }
        } else if (getUser().getCsmUser().getUserId() == null) {
            authorizationManager.createUser(getUser().getCsmUser());
        } else {
            authorizationManager.modifyUser(getUser().getCsmUser());
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
        for (Site site : getProvisionableSites()) {
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
            for (ProvisioningRole role : getProvisionableRoles()) {
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

    public void setLookUpBoundUser(boolean lookUpBoundUser) {
        this.lookUpBoundUser = lookUpBoundUser;
    }

    public boolean getUsesLocalPasswords() {
        return authenticationSystem.usesLocalPasswords();
    }

    ////// BOUND PROPERTIES

    /*
     * This array is parsed with the expectation that it will be the JSON-serialized result
     * of calling #roleChanges on the javascript object psc.admin.ProvisionableUser.
     */

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

    private static class StudyJSONObjectComparator extends ScopeComparator<JSONObject> {
        public static final Comparator<? super JSONObject> INSTANCE =
            new StudyJSONObjectComparator();

        @Override
        public String extractScopeIdentifier(JSONObject o) {
            return o.optString("identifier");
        }

        private StudyJSONObjectComparator() { }
    }
}
