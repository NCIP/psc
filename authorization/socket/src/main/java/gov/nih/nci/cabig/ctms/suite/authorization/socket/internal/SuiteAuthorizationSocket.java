/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.Privilege;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElementPrivilegeContext;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.Role;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings( { "RawUseOfParameterizedType", "unchecked" })
public class SuiteAuthorizationSocket extends AuthorizationManagerAdapter {
    private SuiteAuthorizationSource source;
    private IdentifiedNameIndex scopeObjectIndex;

    // CSM domain objects need an update date or they puke on toString
    private static final Date FIXED_UPDATE_DATE = new Date(0);

    // Map from User property to SuiteUserSearchOptions property
    private static final Map<String, String> ALLOWED_USER_SEARCH_CRITERIA =
        new MapBuilder<String, String>().
            put("loginName", "usernameSubstring").
            put("firstName", "firstNameSubstring").
            put("lastName", "lastNameSubstring").
            toMap();

    public SuiteAuthorizationSocket(SuiteAuthorizationSource source) {
        this.source = source;
        this.scopeObjectIndex = new IdentifiedNameIndex();
    }

    @Override
    public List getObjects(SearchCriteria searchCriteria) {
        if (searchCriteria.getObjectType().isAssignableFrom(Role.class)) {
            return getRoles(searchCriteria);
        } else if (searchCriteria.getObjectType().isAssignableFrom(Group.class)) {
            return getGroups(searchCriteria);
        } else if (searchCriteria.getObjectType().isAssignableFrom(ProtectionGroup.class)) {
            return getProtectionGroups(searchCriteria);
        } else if (searchCriteria.getObjectType().isAssignableFrom(ProtectionElement.class)) {
            return getProtectionElements(searchCriteria);
        } else if (searchCriteria.getObjectType().isAssignableFrom(User.class)) {
            return getUsers(searchCriteria);
        } else {
            throw new UnsupportedOperationException(
                "Unsupported criteria object type: " + searchCriteria.getObjectType().getSimpleName());
        }
    }

    private List getRoles(SearchCriteria searchCriteria) {
        String nameCriterion = (String) searchCriteria.getFieldAndValues().get("name");
        screenOutLikeCriterion(nameCriterion, searchCriteria.getObjectType());
        try {
            SuiteRole role = SuiteRole.getByCsmName(nameCriterion);
            return Arrays.asList(createCsmRole(role));
        } catch (IllegalArgumentException iae) {
            return Collections.emptyList();
        }
    }

    private Role createCsmRole(SuiteRole suiteRole) {
        Role r = new Role();
        r.setId(csmGroupOrRoleIdForSuiteRole(suiteRole));
        r.setName(suiteRole.getCsmName());
        r.setUpdateDate(FIXED_UPDATE_DATE);
        return r;
    }

    private List getGroups(SearchCriteria searchCriteria) {
        String nameCriterion = (String) searchCriteria.getFieldAndValues().get("groupName");
        screenOutLikeCriterion(nameCriterion, searchCriteria.getObjectType());
        try {
            SuiteRole role = SuiteRole.getByCsmName(nameCriterion);
            return Arrays.asList(createCsmGroup(role));
        } catch (IllegalArgumentException iae) {
            return Collections.emptyList();
        }
    }

    private Group createCsmGroup(SuiteRole suiteRole) {
        Group g = new Group();
        g.setGroupId(csmGroupOrRoleIdForSuiteRole(suiteRole));
        g.setGroupName(suiteRole.getCsmName());
        g.setUpdateDate(FIXED_UPDATE_DATE);
        return g;
    }

    private List getProtectionGroups(SearchCriteria searchCriteria) {
        String nameCriterion = (String) searchCriteria.getFieldAndValues().get("protectionGroupName");
        screenOutLikeCriterion(nameCriterion, searchCriteria.getObjectType());

        return Arrays.asList(createCsmProtectionGroup(nameCriterion));
    }

    private ProtectionGroup createCsmProtectionGroup(String name) {
        ProtectionGroup pg = new ProtectionGroup();
        pg.setProtectionGroupId(scopeObjectIndex.get(name).getId());
        pg.setProtectionGroupName(name);
        pg.setUpdateDate(FIXED_UPDATE_DATE);
        return pg;
    }

    private List getProtectionElements(SearchCriteria searchCriteria) {
        String nameCriterion = (String) searchCriteria.getFieldAndValues().get("objectId");
        if (nameCriterion == null) {
            nameCriterion = (String) searchCriteria.getFieldAndValues().get("protectionElementName");
        }
        screenOutLikeCriterion(nameCriterion, searchCriteria.getObjectType());

        return Arrays.asList(createCsmProtectionElement(nameCriterion));
    }

    private ProtectionElement createCsmProtectionElement(String objectId) {
        ProtectionElement pg = new ProtectionElement();
        pg.setProtectionElementId(scopeObjectIndex.get(objectId).getId());
        pg.setObjectId(objectId);
        pg.setProtectionElementName(objectId);
        pg.setUpdateDate(FIXED_UPDATE_DATE);
        return pg;
    }

    private void screenOutLikeCriterion(String nameCriterion, Class objectType) {
        if (nameCriterion.contains("%")) {
            throw new UnsupportedOperationException(
                "Unsupported criterion: only exact matches are supported for " +
                    objectType.getSimpleName());
        }
    }

    @Override
    public Set getProtectionElements(String protectionGroupId) throws CSObjectNotFoundException {
        long pgId = new Long(protectionGroupId);
        IdentifiedName known = scopeObjectIndex.get(pgId);
        if (known == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(createCsmProtectionElement(known.getName()));
        }
    }

    @Override
    public ProtectionElement getProtectionElement(String objectId) throws CSObjectNotFoundException {
        return createCsmProtectionElement(objectId);
    }

    @Override
    public User getUser(String loginName) {
        SuiteUser match = source.getUser(loginName, SuiteUserRoleLevel.NONE);
        return match == null ? null : createCsmUser(match);
    }

    @Override
    public User getUserById(String userId) throws CSObjectNotFoundException {
        int id = new Integer(userId);
        SuiteUser match = source.getUser(id, SuiteUserRoleLevel.NONE);
        if (match == null) {
            throw userNotFound(userId);
        } else {
            return createCsmUser(match);
        }
    }

    private List getUsers(SearchCriteria searchCriteria) {
        String loginNameCriterion = (String) searchCriteria.getFieldAndValues().get("loginName");
        if (loginNameCriterion != null && !loginNameCriterion.contains("%")) {
            return Collections.singletonList(getUser(loginNameCriterion));
        }
        SuiteUserSearchOptions opts = createUserSearchOptions(searchCriteria.getFieldAndValues());

        return createCsmUserList(source.searchUsers(opts));
    }

    private SuiteUserSearchOptions createUserSearchOptions(Map<String, String> criteria) {
        SuiteUserSearchOptions opts = new SuiteUserSearchOptions();
        BeanWrapper optsWrapper = new BeanWrapperImpl(opts);
        Map<String, String> remainingCriteria = new HashMap<String, String>(criteria);
        for (String userProp : ALLOWED_USER_SEARCH_CRITERIA.keySet()) {
            String criterion = remainingCriteria.remove(userProp);
            if (criterion == null) continue;

            if (isSubstringLikeCriterion(criterion)) {
                String substring = criterion.replaceAll("%", "");
                if (StringUtils.hasText(substring)) {
                    optsWrapper.setPropertyValue(ALLOWED_USER_SEARCH_CRITERIA.get(userProp),
                        substring);
                }
            } else {
                StringBuilder msg = new StringBuilder().
                    append("Unsupported criterion: only substring");
                if ("loginName".equals(userProp)) msg.append(" or exact");
                msg.append(" matches are supported for User#").append(userProp);

                throw new UnsupportedOperationException(msg.toString());
            }
        }

        if (remainingCriteria.isEmpty()) {
            return opts;
        } else {
            throw new UnsupportedOperationException("Unsupported criterion: " +
                StringUtils.collectionToDelimitedString(remainingCriteria.keySet(), ", ") +
                " searches not supported for User");
        }
    }

    /**
     * Returns true if the string both starts and ends with '%'
     */
    private boolean isSubstringLikeCriterion(String criterion) {
        return criterion.charAt(0) == '%' &&
            criterion.charAt(criterion.length() - 1) == '%';
    }

    @Override
    public Set getUsers(String groupId) throws CSObjectNotFoundException {
        SuiteRole role = suiteRoleForCsmGroupOrRoleId(new Long(groupId));

        return createCsmUserSet(source.getUsersByRole(role));
    }

    @Override
    public Set getGroups(String userId) throws CSObjectNotFoundException {
        int id = new Integer(userId);
        SuiteUser match = source.getUser(id, SuiteUserRoleLevel.ROLES);
        if (match == null) {
            throw userNotFound(userId);
        } else {
            Set<Group> groups = new LinkedHashSet<Group>();
            for (SuiteRole suiteRole : match.getRoleMemberships().keySet()) {
                groups.add(createCsmGroup(suiteRole));
            }
            return groups;
        }
    }

    @Override
    public Set getProtectionElementPrivilegeContextForUser(String userId) throws CSObjectNotFoundException {
        int id = new Integer(userId);
        SuiteUser match = source.getUser(id, SuiteUserRoleLevel.ROLES_AND_SCOPES);
        if (match == null) {
            throw userNotFound(userId);
        } else {
            Map<String, ProtectionElementPrivilegeContext> contexts =
                new HashMap<String, ProtectionElementPrivilegeContext>();
            for (SuiteRoleMembership srm : match.getRoleMemberships().values()) {
                mergeRoleMembershipIntoContext(contexts, srm);
            }
            return new HashSet<ProtectionElementPrivilegeContext>(contexts.values());
        }
    }

    private void mergeRoleMembershipIntoContext(
        Map<String, ProtectionElementPrivilegeContext> contexts, SuiteRoleMembership srm
    ) {
        List<String> protectionElementObjectIds = new ArrayList<String>();
        for (ScopeType scopeType : ScopeType.values()) {
            if (srm.hasScope(scopeType)) {
                if (srm.isAll(scopeType)) {
                    protectionElementObjectIds.add(scopeType.getAllScopeCsmName());
                } else {
                    for (String identifier : srm.getIdentifiers(scopeType)) {
                        protectionElementObjectIds.add(scopeType.getScopeCsmNamePrefix() + identifier);
                    }
                }
            }
        }

        Privilege csmPrivilege = createCsmPrivilege(srm.getRole());
        for (String protectionElementObjectId : protectionElementObjectIds) {
            if (!contexts.containsKey(protectionElementObjectId)) {
                ProtectionElement pe = createCsmProtectionElement(protectionElementObjectId);
                ProtectionElementPrivilegeContext ctxt = new ProtectionElementPrivilegeContext();
                ctxt.setProtectionElement(pe);
                ctxt.setPrivileges(new LinkedHashSet<Privilege>());
                contexts.put(protectionElementObjectId, ctxt);
            }

            contexts.get(protectionElementObjectId).getPrivileges().add(csmPrivilege);
        }
    }

    private Privilege createCsmPrivilege(SuiteRole suiteRole) {
        Privilege p = new Privilege();
        p.setId((long) suiteRole.ordinal());
        p.setName(suiteRole.getCsmName());
        return p;
    }

    private CSObjectNotFoundException userNotFound(String userId) {
        return new CSObjectNotFoundException("No user with ID " + userId + " in the source");
    }

    private User createCsmUser(SuiteUser match) {
        User user = new User();
        user.setLoginName(match.getUsername());
        user.setUserId(match.getId() == null ? null : match.getId().longValue());
        user.setFirstName(match.getFirstName());
        user.setLastName(match.getLastName());
        user.setEmailId(match.getEmailAddress());
        user.setEndDate(match.getAccountEndDate());
        user.setUpdateDate(FIXED_UPDATE_DATE);
        return user;
    }

    private List<User> createCsmUserList(Collection<SuiteUser> suiteUsers) {
        List<User> users = new ArrayList<User>(suiteUsers.size());
        for (SuiteUser suiteUser : suiteUsers) users.add(createCsmUser(suiteUser));
        return users;
    }

    private Set<User> createCsmUserSet(Collection<SuiteUser> suiteUsers) {
        Set<User> users = new LinkedHashSet<User>();
        for (SuiteUser suiteUser : suiteUsers) users.add(createCsmUser(suiteUser));
        return users;
    }

    private long csmGroupOrRoleIdForSuiteRole(SuiteRole suiteRole) {
        return (long) suiteRole.ordinal();
    }

    private SuiteRole suiteRoleForCsmGroupOrRoleId(long id) {
        if (id >= SuiteRole.values().length) {
            throw new IllegalArgumentException(id + " does not correspond to a SuiteRole");
        }
        return SuiteRole.values()[((int) id)];
    }
}
