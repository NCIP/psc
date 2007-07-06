package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.exceptions.CSTransactionException;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSException;
import gov.nih.nci.security.authorization.domainobjects.*;
import gov.nih.nci.security.authorization.jaas.AccessPermission;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;

import javax.security.auth.Subject;
import java.util.*;
import java.security.Principal;

public class UserServiceTest extends StudyCalendarTestCase {
    private UserDao userDao;
    private UserProvisioningManager userProvisioningManager;
    private TestUserProvisioningManager testUserProvisioningManager;
    private UserService service;
    private List<Group> allCsmGroups;


    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerMockFor(UserDao.class);
        userProvisioningManager = registerMockFor(UserProvisioningManager.class);
        testUserProvisioningManager = new TestUserProvisioningManager();

        service = new UserService();
        service.setUserDao(userDao);
        service.setUserProvisioningManager(userProvisioningManager);

        allCsmGroups = new ArrayList<Group>();
        allCsmGroups.add(createCsmGroup(1L, "STUDY_COORDINATOR"));
        allCsmGroups.add(createCsmGroup(2L, "STUDY_ADMIN"));
        allCsmGroups.add(createCsmGroup(3L, "PARTICIPANT_COORDINATOR"));
        allCsmGroups.add(createCsmGroup(5L, "RESEARCH_ASSOCIATE"));
        allCsmGroups.add(createCsmGroup(6L, "SITE_COORDINATOR"));
    }

    public void testSaveUser() throws Exception {
        service.setUserProvisioningManager(testUserProvisioningManager);
        User expectedUser = createUser(200, "john", 100L, new Role[] {Role.STUDY_ADMIN, Role.STUDY_COORDINATOR}, true);
        String[] expectedCsmGroups = {"2", "6"};

        gov.nih.nci.security.authorization.domainobjects.User expectedCsmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        expectedCsmUser.setLoginName("john");
        expectedCsmUser.setUserId(1L);

        testUserProvisioningManager.createUser(expectedCsmUser);

        testUserProvisioningManager.assignGroupsToUser("100", expectedCsmGroups);
        userDao.save(expectedUser);
        replayMocks();

        User actual = service.saveUser(expectedUser);
        verifyMocks();

        assertEquals(expectedUser, actual);
    }

     public void testSaveUser_2() throws Exception {
        service.setUserProvisioningManager(testUserProvisioningManager);
        User expectedUser = createUser(200, "john", 100L, null, false);
        String[] expectedCsmGroups = {};

        gov.nih.nci.security.authorization.domainobjects.User expectedCsmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        expectedCsmUser.setLoginName("john");
        expectedCsmUser.setUserId(1L);

        testUserProvisioningManager.createUser(expectedCsmUser);

        testUserProvisioningManager.assignGroupsToUser("100", expectedCsmGroups);
        userDao.save(expectedUser);
        replayMocks();

        User actual = service.saveUser(expectedUser);
        verifyMocks();

        assertEquals(expectedUser, actual);
    }


    public void testGetUserByName() throws Exception {
        User expectedUser = createUser(-100, "john", -100L, null, true);
        List expectedUsers = Collections.singletonList(expectedUser);

        expect(userDao.getByName(expectedUser.getName())).andReturn(expectedUsers);
        replayMocks();

        User actualUser = service.getUserByName("john");
        verifyMocks();

        assertEquals(expectedUser, actualUser);
    }

    public void testGetUserById() throws Exception {
        User expectedUser = createUser(-200, "john", -100L, new Role[] {Role.STUDY_ADMIN, Role.STUDY_COORDINATOR}, false);

        expect(userDao.getById(-200)).andReturn(expectedUser);
        replayMocks();

        User actualUser = service.getUserById(-200);
        verifyMocks();

        assertEquals(expectedUser, actualUser);
    }

    // Since roles are stored in Set, order changes.  Write custom comparator to sort and compare
    /*public void testGetByIdAndSave() throws Exception {
        User expectedUser = createUser(-100,"john", -200L, new Role[] {Role.STUDY_ADMIN, Role.SITE_COORDINATOR}, true);
        User expectedUpdatedUser = createUser(-100, "updated", -200L, new Role[] {Role.STUDY_ADMIN, Role.SITE_COORDINATOR}, true);
        String[] expectedCsmGroups = new String[] {"6", "2"};

        expect(userDao.getById(-100)).andReturn(expectedUser);
        expect(userProvisioningManager.getObjects(eqCsmGroupSearchCriteria(new GroupSearchCriteria(new Group())))).andReturn(allCsmGroups);
        userProvisioningManager.assignGroupsToUser(eq("-200"), aryEq(expectedCsmGroups));
        //userProvisioningManager.assignGroupsToUser("-200", expectedCsmGroups);
        userDao.save(expectedUpdatedUser);

        replayMocks();

        User actual = service.getUserById(-100);
        actual.setName("updated");
        service.saveUser(actual);
        verifyMocks();

        assertEquals(expectedUser, actual);
    } */

    public void testIsGroupEqualToRole() {
        UserService us = new UserService();
        assertTrue(us.isGroupEqualToRole(createCsmGroup(1L, "STUDY_COORDINATOR"), Role.STUDY_COORDINATOR));
        assertFalse(us.isGroupEqualToRole(createCsmGroup(1L, "STUDY_COORDINATOR"), Role.SITE_COORDINATOR));
    }

    public static gov.nih.nci.security.authorization.domainobjects.User
            eqCsmUser(gov.nih.nci.security.authorization.domainobjects.User user) {
        EasyMock.reportMatcher(new CsmUserMatcher(user));
        return null;
    }

    public static GroupSearchCriteria
            eqCsmGroupSearchCriteria(GroupSearchCriteria group) {
        EasyMock.reportMatcher(new CsmGroupSearchCriteriaMatcher(group));
        return null;
    }
    
    private static class CsmUserMatcher implements IArgumentMatcher {
        private gov.nih.nci.security.authorization.domainobjects.User expectedUser;

        public CsmUserMatcher(gov.nih.nci.security.authorization.domainobjects.User expectedUser) {
            this.expectedUser = expectedUser;
        }

        public boolean matches(Object object) {
            if(!(object instanceof gov.nih.nci.security.authorization.domainobjects.User)) {
                return false;
            }

            gov.nih.nci.security.authorization.domainobjects.User actual =
                    (gov.nih.nci.security.authorization.domainobjects.User) object;

            if (expectedUser.getLoginName() != null ?
                    !expectedUser.getLoginName().equals(actual.getLoginName()) :
                    actual.getLoginName() != null)
                return false;


            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("User with login name=").append(expectedUser.getLoginName());
        }
    }

    private static class CsmGroupSearchCriteriaMatcher implements IArgumentMatcher {
        private GroupSearchCriteria expectedGroup;

        public CsmGroupSearchCriteriaMatcher(GroupSearchCriteria expectedGroup) {
            this.expectedGroup = expectedGroup;
        }

        public boolean matches(Object object) {
            if(!(object instanceof GroupSearchCriteria)) {
                return false;
            }

            GroupSearchCriteria actual =
                    (GroupSearchCriteria) object;

            if (expectedGroup.getMessage() != null ?
                    !expectedGroup.getMessage().equals(actual.getMessage()) :
                    actual.getMessage() != null)
                return false;


            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("Group with message =").append(expectedGroup.getMessage());
        }
    }

    private User createUser(Integer id, String name, Long csmUserId, Role[] roles, boolean activeFlag) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setCsmUserId(csmUserId);
        user.setActiveFlag(new Boolean(activeFlag));
        if(roles != null) {
            user.setRoles(new HashSet<Role>());
            Collections.addAll(user.getRoles(), roles); 
        }

        return user;
    }

    private Group createCsmGroup(Long id, String name) {
        Group g = new Group();
        g.setGroupId(id);
        g.setGroupName(name);
        return g;
    }

    private class TestUserProvisioningManager implements UserProvisioningManager {


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

        public void assignGroupsToUser(String string, String[] strings) throws CSTransactionException {
            // assignGroupsToUser
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

        public List getObjects(SearchCriteria searchCriteria) {
            return allCsmGroups;
        }

        public void createUser(gov.nih.nci.security.authorization.domainobjects.User user) throws CSTransactionException {
            user.setUserId(1L);
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

        public Application getApplicationById(String string) throws CSObjectNotFoundException {
            Application ap = new Application();
            return ap;
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
    }

}
