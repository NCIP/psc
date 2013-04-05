/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.Application;
import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
import gov.nih.nci.security.authorization.domainobjects.FilterClause;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.InstanceLevelMappingElement;
import gov.nih.nci.security.authorization.domainobjects.Privilege;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.Role;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.authorization.jaas.AccessPermission;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.exceptions.CSDataAccessException;
import gov.nih.nci.security.exceptions.CSException;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSTransactionException;

import javax.security.auth.Subject;
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An empty implementation of CSM's {@link AuthorizationManager}.
 * Separated out from {@link SuiteAuthorizationSocket} for clarity. The socket only overrides those
 * methods that a suite application is expected to call.
 *
 * @author Rhett Sutphin
 */
@SuppressWarnings( { "RawUseOfParameterizedType", "unchecked" })
class AuthorizationManagerAdapter implements AuthorizationManager {
    protected Object handleNotImplemented(String methodName) {
        throw new UnsupportedOperationException(methodName + " not implemented");
    }

    public User getUser(String s) {
        return (User) handleNotImplemented("getUser");
    }

    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) handleNotImplemented("getApplicationContext");
    }

    public void assignProtectionElement(String s, String s1, String s2) throws CSTransactionException {
        handleNotImplemented("assignProtectionElement");
    }

    public void setOwnerForProtectionElement(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("setOwnerForProtectionElement");
    }

    public void removeOwnerForProtectionElement(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("removeOwnerForProtectionElement");
    }

    public void deAssignProtectionElements(String s, String s1) throws CSTransactionException {
        handleNotImplemented("deAssignProtectionElements");
    }

    public void createProtectionElement(ProtectionElement protectionElement) throws CSTransactionException {
        handleNotImplemented("createProtectionElement");
    }

    public boolean checkPermission(AccessPermission accessPermission, Subject subject) throws CSException {
        return (Boolean) handleNotImplemented("checkPermission");
    }

    public boolean checkPermission(AccessPermission accessPermission, String s) throws CSException {
        return (Boolean) handleNotImplemented("checkPermission");
    }

    public boolean checkPermission(String s, String s1, String s2, String s3) throws CSException {
        return (Boolean) handleNotImplemented("checkPermission");
    }

    public boolean checkPermission(String s, String s1, String s2, String s3, String s4) throws CSException {
        return (Boolean) handleNotImplemented("checkPermission");
    }

    public boolean checkPermission(String s, String s1, String s2) throws CSException {
        return (Boolean) handleNotImplemented("checkPermission");
    }

    public boolean checkPermissionForGroup(String s, String s1, String s2, String s3) throws CSException {
        return (Boolean) handleNotImplemented("checkPermissionForGroup");
    }

    public boolean checkPermissionForGroup(String s, String s1, String s2, String s3, String s4) throws CSException {
        return (Boolean) handleNotImplemented("checkPermissionForGroup");
    }

    public boolean checkPermissionForGroup(String s, String s1, String s2) throws CSException {
        return (Boolean) handleNotImplemented("checkPermissionForGroup");
    }

    public List getAccessibleGroups(String s, String s1) throws CSException {
        return (List) handleNotImplemented("getAccessibleGroups");
    }

    public List getAccessibleGroups(String s, String s1, String s2) throws CSException {
        return (List) handleNotImplemented("getAccessibleGroups");
    }

    public Principal[] getPrincipals(String s) {
        return (Principal[]) handleNotImplemented("getPrincipals");
    }

    public ProtectionElement getProtectionElement(String s) throws CSObjectNotFoundException {
        return (ProtectionElement) handleNotImplemented("getProtectionElement");
    }

    public ProtectionElement getProtectionElementById(String s) throws CSObjectNotFoundException {
        return (ProtectionElement) handleNotImplemented("getProtectionElementById");
    }

    public void assignProtectionElement(String s, String s1) throws CSTransactionException {
        handleNotImplemented("assignProtectionElement");
    }

    public void setOwnerForProtectionElement(String s, String s1, String s2) throws CSTransactionException {
        handleNotImplemented("setOwnerForProtectionElement");
    }

    public void removeOwnerForProtectionElement(String s, String s1, String s2) throws CSTransactionException {
        handleNotImplemented("removeOwnerForProtectionElement");
    }

    public void initialize(String s) {
        handleNotImplemented("initialize");
    }

    public void initialize(String s, URL url) {
        handleNotImplemented("initialize");
    }

    public List getProtectionGroups() {
        return (List) handleNotImplemented("getProtectionGroups");
    }

    public ProtectionElement getProtectionElement(String s, String s1) {
        return (ProtectionElement) handleNotImplemented("getProtectionElement");
    }

    public Object secureObject(String s, Object o) throws CSException {
        return handleNotImplemented("secureObject");
    }

    public Collection secureCollection(String s, Collection collection) throws CSException {
        return (Collection) handleNotImplemented("secureCollection");
    }

    public Set getProtectionGroups(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getProtectionGroups");
    }

    public Collection getPrivilegeMap(String s, Collection collection) throws CSException {
        return (Collection) handleNotImplemented("getPrivilegeMap");
    }

    public Object secureUpdate(String s, Object o, Object o1) throws CSException {
        return handleNotImplemented("secureUpdate");
    }

    public boolean checkOwnership(String s, String s1) {
        return (Boolean) handleNotImplemented("checkOwnership");
    }

    public void setAuditUserInfo(String s, String s1) {
        handleNotImplemented("setAuditUserInfo");
    }

    public void setEncryptionEnabled(boolean b) {
        handleNotImplemented("setEncryptionEnabled");
    }

    public Application getApplication(String s) throws CSObjectNotFoundException {
        return (Application) handleNotImplemented("getApplication");
    }

    public void createProtectionGroup(ProtectionGroup protectionGroup) throws CSTransactionException {
        handleNotImplemented("createProtectionGroup");
    }

    public void modifyProtectionGroup(ProtectionGroup protectionGroup) throws CSTransactionException {
        handleNotImplemented("modifyProtectionGroup");
    }

    public void removeProtectionGroup(String s) throws CSTransactionException {
        handleNotImplemented("removeProtectionGroup");
    }

    public void removeProtectionElement(String s) throws CSTransactionException {
        handleNotImplemented("removeProtectionElement");
    }

    public void addUserRoleToProtectionGroup(String s, String[] strings, String s1) throws CSTransactionException {
        handleNotImplemented("addUserRoleToProtectionGroup");
    }

    public void assignUserRoleToProtectionGroup(String s, String[] strings, String s1) throws CSTransactionException {
        handleNotImplemented("assignUserRoleToProtectionGroup");
    }

    public void removeUserRoleFromProtectionGroup(String s, String s1, String[] strings) throws CSTransactionException {
        handleNotImplemented("removeUserRoleFromProtectionGroup");
    }

    public void createRole(Role role) throws CSTransactionException {
        handleNotImplemented("createRole");
    }

    public void modifyRole(Role role) throws CSTransactionException {
        handleNotImplemented("modifyRole");
    }

    public void removeRole(String s) throws CSTransactionException {
        handleNotImplemented("removeRole");
    }

    public void createPrivilege(Privilege privilege) throws CSTransactionException {
        handleNotImplemented("createPrivilege");
    }

    public void modifyPrivilege(Privilege privilege) throws CSTransactionException {
        handleNotImplemented("modifyPrivilege");
    }

    public void removePrivilege(String s) throws CSTransactionException {
        handleNotImplemented("removePrivilege");
    }

    public void addPrivilegesToRole(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("addPrivilegesToRole");
    }

    public void assignPrivilegesToRole(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignPrivilegesToRole");
    }

    public void createGroup(Group group) throws CSTransactionException {
        handleNotImplemented("createGroup");
    }

    public void removeGroup(String s) throws CSTransactionException {
        handleNotImplemented("removeGroup");
    }

    public void modifyGroup(Group group) throws CSTransactionException {
        handleNotImplemented("modifyGroup");
    }

    public void assignGroupsToUser(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignGroupsToUser");
    }

    public void addGroupsToUser(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("addGroupsToUser");
    }

    public void assignUsersToGroup(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignUsersToGroup");
    }

    public void addUsersToGroup(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("addUsersToGroup");
    }

    public void assignUserToGroup(String s, String s1) throws CSTransactionException {
        handleNotImplemented("assignUserToGroup");
    }

    public void removeUserFromGroup(String s, String s1) throws CSTransactionException {
        handleNotImplemented("removeUserFromGroup");
    }

    public void addGroupRoleToProtectionGroup(String s, String s1, String[] strings) throws CSTransactionException {
        handleNotImplemented("addGroupRoleToProtectionGroup");
    }

    public void assignGroupRoleToProtectionGroup(String s, String s1, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignGroupRoleToProtectionGroup");
    }

    public Privilege getPrivilegeById(String s) throws CSObjectNotFoundException {
        return (Privilege) handleNotImplemented("getPrivilegeById");
    }

    public void removeUserFromProtectionGroup(String s, String s1) throws CSTransactionException {
        handleNotImplemented("removeUserFromProtectionGroup");
    }

    public void removeGroupRoleFromProtectionGroup(String s, String s1, String[] strings) throws CSTransactionException {
        handleNotImplemented("removeGroupRoleFromProtectionGroup");
    }

    public void removeGroupFromProtectionGroup(String s, String s1) throws CSTransactionException {
        handleNotImplemented("removeGroupFromProtectionGroup");
    }

    public Role getRoleById(String s) throws CSObjectNotFoundException {
        return (Role) handleNotImplemented("getRoleById");
    }

    public Set getPrivileges(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getPrivileges");
    }

    public List getObjects(SearchCriteria searchCriteria) {
        return (List) handleNotImplemented("getObjects");
    }

    public void createUser(User user) throws CSTransactionException {
        handleNotImplemented("createUser");
    }

    public ProtectionGroup getProtectionGroupById(String s) throws CSObjectNotFoundException {
        return (ProtectionGroup) handleNotImplemented("getProtectionGroupById");
    }

    public void assignProtectionElements(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignProtectionElements");
    }

    public void addProtectionElements(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("addProtectionElements");
    }

    public void removeProtectionElementsFromProtectionGroup(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("removeProtectionElementsFromProtectionGroup");
    }

    public Set getProtectionGroupRoleContextForUser(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getProtectionGroupRoleContextForUser");
    }

    public Set getProtectionGroupRoleContextForGroup(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getProtectionGroupRoleContextForGroup");
    }

    public Set getProtectionElementPrivilegeContextForUser(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getProtectionElementPrivilegeContextForUser");
    }

    public Set getProtectionElementPrivilegeContextForGroup(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getProtectionElementPrivilegeContextForGroup");
    }

    public Group getGroupById(String s) throws CSObjectNotFoundException {
        return (Group) handleNotImplemented("getGroupById");
    }

    public void modifyProtectionElement(ProtectionElement protectionElement) throws CSTransactionException {
        handleNotImplemented("modifyProtectionElement");
    }

    public User getUserById(String s) throws CSObjectNotFoundException {
        return (User) handleNotImplemented("getUserById");
    }

    public Set getUsers(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getUsers");
    }

    public void modifyUser(User user) throws CSTransactionException {
        handleNotImplemented("modifyUser");
    }

    public void removeUser(String s) throws CSTransactionException {
        handleNotImplemented("removeUser");
    }

    public Set getGroups(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getGroups");
    }

    public Set getProtectionElements(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getProtectionElements");
    }

    public void addToProtectionGroups(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("addToProtectionGroups");
    }

    public void assignToProtectionGroups(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignToProtectionGroups");
    }

    public void assignParentProtectionGroup(String s, String s1) throws CSTransactionException {
        handleNotImplemented("assignParentProtectionGroup");
    }

    public void createApplication(Application application) throws CSTransactionException {
        handleNotImplemented("createApplication");
    }

    public void modifyApplication(Application application) throws CSTransactionException {
        handleNotImplemented("modifyApplication");
    }

    public void removeApplication(String s) throws CSTransactionException {
        handleNotImplemented("removeApplication");
    }

    public Application getApplicationById(String s) throws CSObjectNotFoundException {
        return (Application) handleNotImplemented("getApplicationById");
    }

    public void assignOwners(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("assignOwners");
    }

    public void addOwners(String s, String[] strings) throws CSTransactionException {
        handleNotImplemented("addOwners");
    }

    public Set getOwners(String s) throws CSObjectNotFoundException {
        return (Set) handleNotImplemented("getOwners");
    }

    public List getAttributeMap(String s, String s1, String s2) {
        return (List) handleNotImplemented("getAttributeMap");
    }

    public List getAttributeMapForGroup(String s, String s1, String s2) {
        return (List) handleNotImplemented("getAttributeMapForGroup");
    }

    public void createFilterClause(FilterClause filterClause) throws CSTransactionException {
        handleNotImplemented("createFilterClause");
    }

    public FilterClause getFilterClauseById(String s) throws CSObjectNotFoundException {
        return (FilterClause) handleNotImplemented("getFilterClauseById");
    }

    public void modifyFilterClause(FilterClause filterClause) throws CSTransactionException {
        handleNotImplemented("modifyFilterClause");
    }

    public void removeFilterClause(String s) throws CSTransactionException {
        handleNotImplemented("removeFilterClause");
    }

    public void createInstanceLevelMappingElement(InstanceLevelMappingElement instanceLevelMappingElement) throws CSTransactionException {
        handleNotImplemented("createInstanceLevelMappingElement");
    }

    public InstanceLevelMappingElement getInstanceLevelMappingElementById(String s) throws CSObjectNotFoundException {
        return (InstanceLevelMappingElement) handleNotImplemented("getInstanceLevelMappingElementById");
    }

    public void modifyInstanceLevelMappingElement(InstanceLevelMappingElement instanceLevelMappingElement) throws CSTransactionException {
        handleNotImplemented("modifyInstanceLevelMappingElement");
    }

    public void removeInstanceLevelMappingElement(String s) throws CSTransactionException {
        handleNotImplemented("removeInstanceLevelMappingElement");
    }

    public void maintainInstanceTables(String s) throws CSObjectNotFoundException, CSDataAccessException {
        handleNotImplemented("maintainInstanceTables");
    }

    public void refreshInstanceTables(boolean b) throws CSObjectNotFoundException, CSDataAccessException {
        handleNotImplemented("refreshInstanceTables");
    }
}
