package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.Application;
import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
import gov.nih.nci.security.authorization.domainobjects.FilterClause;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.InstanceLevelMappingElement;
import gov.nih.nci.security.authorization.domainobjects.Privilege;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
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

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static java.util.Arrays.asList;
import static org.easymock.classextension.EasyMock.expect;

public class UserServiceTest extends StudyCalendarTestCase {
    private UserDao userDao;
    private AuthorizationManager authorizationManager;
    private AuthorizationManagerStub authorizationManagerStub;
    private UserService service;
    private SiteConsumer siteConsumer;
    private User adam, steve, siteCoord;
    private Site nu, mayo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerMockFor(UserDao.class);
        authorizationManager = registerMockFor(AuthorizationManager.class);
        authorizationManagerStub = new AuthorizationManagerStub();
        siteConsumer = registerMockFor(SiteConsumer.class);

        service = new UserService();
        service.setUserDao(userDao);
        service.setAuthorizationManager(authorizationManager);
        service.setSiteConsumer(siteConsumer);
        
        adam = createNamedInstance("Adam", User.class);
        steve = createNamedInstance("Steve", User.class);
        siteCoord = createNamedInstance("Site Coordinator", User.class);

        nu = createNamedInstance("Northwestern", Site.class);
        mayo = createNamedInstance("Mayo Clinic", Site.class);

        createUserRole(adam, Role.SUBJECT_COORDINATOR, nu, mayo);
        createUserRole(steve, Role.SUBJECT_COORDINATOR, mayo);
        createUserRole(siteCoord, Role.SITE_COORDINATOR, nu);
    }

    public void testSaveNewUser() throws Exception {
        service.setAuthorizationManager(authorizationManagerStub);
        User expectedUser = createUser(null, "john", null, true, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);

        userDao.save(expectedUser);
        replayMocks();

        User actual = service.saveUser(expectedUser, "flan", null);
        verifyMocks();

        assertSame("Input user not returned", expectedUser, actual);
        assertEquals("CSM ID not propagated to PSC user", (Long) AuthorizationManagerStub.USER_ID,
            actual.getCsmUserId());
        assertEquals("CSM user not given correct login name", expectedUser.getName(),
            authorizationManagerStub.getCreatedUser().getLoginName());
        assertEquals("CSM user not given correct password", "flan",
            authorizationManagerStub.getCreatedUser().getPassword());
    }

    public void testGetUserByNameReturnNullForUnknown() throws Exception {
        expect(userDao.getByName("joe")).andReturn(null);
        replayMocks();

        assertNull(service.getUserByName("joe"));
        verifyMocks();
    }

    public void testGeyUserByNameRefreshesAssociatedSites() throws Exception {
        expect(userDao.getByName("adam")).andReturn(adam);
        expect(siteConsumer.refresh(asList(nu, mayo))).andReturn(asList(nu, mayo));
        replayMocks();

        assertSame(adam, service.getUserByName("adam"));
        verifyMocks();
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private class AuthorizationManagerStub implements AuthorizationManager {
        public static final long USER_ID = 100L;
        private gov.nih.nci.security.authorization.domainobjects.User createdUser;

        public gov.nih.nci.security.authorization.domainobjects.User getCreatedUser() {
            return createdUser;
        }

        ////// IMPLEMENTATION

        public void createUser(gov.nih.nci.security.authorization.domainobjects.User user) throws CSTransactionException {
            createdUser = user;
            user.setUserId(USER_ID);
        }

        public Application getApplicationById(String string) throws CSObjectNotFoundException {
            return new Application();
        }

        public void assignGroupsToUser(String string, String[] strings) throws CSTransactionException {}

        ////// All remaining methods are unsupported

        public void assignUsersToGroup(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public Set getUsers(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void removeOwnerForProtectionElement(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermissionForGroup(String string, String string1, String string2, String string3) throws CSException {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermissionForGroup(String string, String string1, String string2) throws CSException {
            throw new UnsupportedOperationException();
        }

        public List getAccessibleGroups(String string, String string1) throws CSException {
            throw new UnsupportedOperationException();
        }

        public List getAccessibleGroups(String string, String string1, String string2) throws CSException {
            throw new UnsupportedOperationException();
        }

        public void removeOwnerForProtectionElement(String string, String string1, String string2) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void setEncryptionEnabled(boolean b) {
            throw new UnsupportedOperationException();
        }

        public Application getApplication(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public List getObjects(SearchCriteria searchCriteria) {
            throw new UnsupportedOperationException();
        }

        public void createProtectionGroup(ProtectionGroup protectionGroup) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void modifyProtectionGroup(ProtectionGroup protectionGroup) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeProtectionGroup(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeProtectionElement(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void assignUserRoleToProtectionGroup(String string, String[] strings, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeUserRoleFromProtectionGroup(String string, String string1, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void createRole(gov.nih.nci.security.authorization.domainobjects.Role role) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void modifyRole(gov.nih.nci.security.authorization.domainobjects.Role role) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeRole(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void createPrivilege(Privilege privilege) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void modifyPrivilege(Privilege privilege) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removePrivilege(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void assignPrivilegesToRole(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void createGroup(Group group) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeGroup(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void modifyGroup(Group group) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void assignUserToGroup(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeUserFromGroup(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void assignGroupRoleToProtectionGroup(String string, String string1, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public Privilege getPrivilegeById(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void removeUserFromProtectionGroup(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeGroupRoleFromProtectionGroup(String string, String string1, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeGroupFromProtectionGroup(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public gov.nih.nci.security.authorization.domainobjects.Role getRoleById(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Set getPrivileges(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public ProtectionGroup getProtectionGroupById(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void assignProtectionElements(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeProtectionElementsFromProtectionGroup(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public Set getProtectionGroupRoleContextForUser(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Set getProtectionGroupRoleContextForGroup(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Set getProtectionElementPrivilegeContextForUser(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Set getProtectionElementPrivilegeContextForGroup(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Group getGroupById(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void modifyProtectionElement(ProtectionElement protectionElement) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public gov.nih.nci.security.authorization.domainobjects.User getUserById(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void modifyUser(gov.nih.nci.security.authorization.domainobjects.User user) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeUser(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public Set getGroups(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Set getProtectionElements(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void assignToProtectionGroups(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void assignParentProtectionGroup(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void createApplication(Application application) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void modifyApplication(Application application) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void removeApplication(String string) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void assignOwners(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public Set getOwners(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public gov.nih.nci.security.authorization.domainobjects.User getUser(String string) {
            throw new UnsupportedOperationException();
        }

        public ApplicationContext getApplicationContext() {
            throw new UnsupportedOperationException();
        }

        public void assignProtectionElement(String string, String string1, String string2) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void setOwnerForProtectionElement(String string, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void deAssignProtectionElements(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void createProtectionElement(ProtectionElement protectionElement) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermission(AccessPermission accessPermission, Subject subject) throws CSException {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermission(AccessPermission accessPermission, String string) throws CSException {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermission(String string, String string1, String string2, String string3) throws CSException {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermission(String string, String string1, String string2) throws CSException {
            throw new UnsupportedOperationException();
        }

        public Principal[] getPrincipals(String string) {
            throw new UnsupportedOperationException();
        }

        public ProtectionElement getProtectionElement(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public ProtectionElement getProtectionElementById(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public void assignProtectionElement(String string, String string1) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void setOwnerForProtectionElement(String string, String string1, String string2) throws CSTransactionException {
            throw new UnsupportedOperationException();
        }

        public void initialize(String string) {
            throw new UnsupportedOperationException();
        }

        public List getProtectionGroups() {
            throw new UnsupportedOperationException();
        }

        public ProtectionElement getProtectionElement(String string, String string1) {
            throw new UnsupportedOperationException();
        }

        public Object secureObject(String string, Object object) throws CSException {
            throw new UnsupportedOperationException();
        }

        public Collection secureCollection(String string, Collection collection) throws CSException {
            throw new UnsupportedOperationException();
        }

        public Set getProtectionGroups(String string) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException();
        }

        public Collection getPrivilegeMap(String string, Collection collection) throws CSException {
            throw new UnsupportedOperationException();
        }

        public Object secureUpdate(String string, Object object, Object object1) throws CSException {
            throw new UnsupportedOperationException();
        }

        public boolean checkOwnership(String string, String string1) {
            throw new UnsupportedOperationException();
        }

        public void setAuditUserInfo(String string, String string1) {
            throw new UnsupportedOperationException();
        }

        public boolean checkPermission(String s, String s1, String s2, String s3, String s4) throws CSException {
            throw new UnsupportedOperationException("checkPermission not implemented");
        }

        public boolean checkPermissionForGroup(String s, String s1, String s2, String s3, String s4) throws CSException {
            throw new UnsupportedOperationException("checkPermissionForGroup not implemented");
        }

        public void initialize(String s, URL url) {
            throw new UnsupportedOperationException("initialize not implemented");
        }

        public void addUserRoleToProtectionGroup(String s, String[] strings, String s1) throws CSTransactionException {
            throw new UnsupportedOperationException("addUserRoleToProtectionGroup not implemented");
        }

        public void addPrivilegesToRole(String s, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addPrivilegesToRole not implemented");
        }

        public void addGroupsToUser(String s, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addGroupsToUser not implemented");
        }

        public void addUsersToGroup(String s, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addUsersToGroup not implemented");
        }

        public void addGroupRoleToProtectionGroup(String s, String s1, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addGroupRoleToProtectionGroup not implemented");
        }

        public void addProtectionElements(String s, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addProtectionElements not implemented");
        }

        public void addToProtectionGroups(String s, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addToProtectionGroups not implemented");
        }

        public void addOwners(String s, String[] strings) throws CSTransactionException {
            throw new UnsupportedOperationException("addOwners not implemented");
        }

        public List getAttributeMap(String s, String s1, String s2) {
            throw new UnsupportedOperationException("getAttributeMap not implemented");
        }

        public List getAttributeMapForGroup(String s, String s1, String s2) {
            throw new UnsupportedOperationException("getAttributeMapForGroup not implemented");
        }

        public void createFilterClause(FilterClause filterClause) throws CSTransactionException {
            throw new UnsupportedOperationException("createFilterClause not implemented");
        }

        public FilterClause getFilterClauseById(String s) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException("getFilterClauseById not implemented");
        }

        public void modifyFilterClause(FilterClause filterClause) throws CSTransactionException {
            throw new UnsupportedOperationException("modifyFilterClause not implemented");
        }

        public void removeFilterClause(String s) throws CSTransactionException {
            throw new UnsupportedOperationException("removeFilterClause not implemented");
        }

        public void createInstanceLevelMappingElement(InstanceLevelMappingElement instanceLevelMappingElement) throws CSTransactionException {
            throw new UnsupportedOperationException("createInstanceLevelMappingElement not implemented");
        }

        public InstanceLevelMappingElement getInstanceLevelMappingElementById(String s) throws CSObjectNotFoundException {
            throw new UnsupportedOperationException("getInstanceLevelMappingElementById not implemented");
        }

        public void modifyInstanceLevelMappingElement(InstanceLevelMappingElement instanceLevelMappingElement) throws CSTransactionException {
            throw new UnsupportedOperationException("modifyInstanceLevelMappingElement not implemented");
        }

        public void removeInstanceLevelMappingElement(String s) throws CSTransactionException {
            throw new UnsupportedOperationException("removeInstanceLevelMappingElement not implemented");
        }

        public void maintainInstanceTables(String s) throws CSObjectNotFoundException, CSDataAccessException {
            throw new UnsupportedOperationException("maintainInstanceTables not implemented");
        }

        public void refreshInstanceTables(boolean b) throws CSObjectNotFoundException, CSDataAccessException {
            throw new UnsupportedOperationException("refreshInstanceTables not implemented");
        }
    }
}
