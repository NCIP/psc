package edu.northwestern.bioinformatics.studycalendar.service;

import static java.util.Collections.singleton;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUser;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.*;
import gov.nih.nci.security.authorization.jaas.AccessPermission;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.exceptions.CSException;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import org.easymock.internal.matchers.ArrayEquals;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.*;

public class UserServiceTest extends StudyCalendarTestCase {
    private UserDao userDao;
    private UserProvisioningManager userProvisioningManager;
    private UserProvisioningManagerStub userProvisioningManagerStub;
    private UserService service;
    private List<Group> allCsmGroups;
    private SiteService siteService;


    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerMockFor(UserDao.class);
        userProvisioningManager = registerMockFor(UserProvisioningManager.class);
        userProvisioningManagerStub = new UserProvisioningManagerStub();
        siteService = registerMockFor(SiteService.class);

        service = new UserService();
        service.setUserDao(userDao);
        service.setUserProvisioningManager(userProvisioningManager);
        service.setSiteService(siteService);

        allCsmGroups = new ArrayList<Group>();
        allCsmGroups.add(createCsmGroup(1L, "STUDY_COORDINATOR"));
        allCsmGroups.add(createCsmGroup(2L, "STUDY_ADMIN"));
        allCsmGroups.add(createCsmGroup(3L, "PARTICIPANT_COORDINATOR"));
        allCsmGroups.add(createCsmGroup(5L, "RESEARCH_ASSOCIATE"));
        allCsmGroups.add(createCsmGroup(6L, "SITE_COORDINATOR"));
    }

    public void testSaveUser() throws Exception {
        service.setUserProvisioningManager(userProvisioningManagerStub);
        User expectedUser = createUser(200, "john", 100L, true, "pass123", Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);

        gov.nih.nci.security.authorization.domainobjects.User expectedCsmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        expectedCsmUser.setLoginName(expectedUser.getName());
        expectedCsmUser.setUserId(expectedUser.getCsmUserId());

        userProvisioningManagerStub.createUser(expectedCsmUser);

        expect(siteService.getSitesForUser(expectedCsmUser.getName())).andReturn(new ArrayList<Site>());

        userDao.save(expectedUser);
        replayMocks();

        User actual = service.saveUser(expectedUser);
        verifyMocks();

        assertUserEquals(expectedUser, actual);
    }


    public void testGetUserByName() throws Exception {
        User expectedUser = createUser(-100, "john", -100L, true, "pass123");
//        List expectedUsers = Collections.singletonList(expectedUser);

        expect(userDao.getByName(expectedUser.getName())).andReturn(expectedUser);
        replayMocks();

        User actualUser = service.getUserByName("john");
        verifyMocks();

        assertUserEquals(expectedUser, actualUser);
    }

    public void testGetUserById() throws Exception {
        User expectedUser = createUser(-200, "john", -100L, false, "pass123", Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);

        expect(userDao.getById(-200)).andReturn(expectedUser);
        replayMocks();

        User actualUser = service.getUserById(-200);
        verifyMocks();

        assertUserEquals(expectedUser, actualUser);
    }

    public void testGetByIdAndAddUserRoleAndSave() throws Exception {
        User expectedUser = createUser(-100,"john", 100L, true, "password", Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        UserRole userRole = createUserRole(expectedUser, Role.PARTICIPANT_COORDINATOR,
                createNamedInstance("Mayo Clinic", Site.class),
                createNamedInstance("Northwestern Clinic", Site.class));

        User expectedUpdatedUser = createUser(-100, "updated", 100L, true, "password", Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        expectedUpdatedUser.addUserRole(userRole);

        String[] expectedCsmGroups = new String[] {"1", "2", "3"}; // CSM ids for Study coordinator and study admin

        expect(userDao.getById(-100)).andReturn(expectedUser);
        expect(userProvisioningManager.getObjects(eqCsmGroupSearchCriteria(new GroupSearchCriteria(new Group())))).andReturn(allCsmGroups);
        userProvisioningManager.assignGroupsToUser(eq("100"), unsortedAryEq(expectedCsmGroups));
        expect(siteService.getSitesForUser(expectedUpdatedUser.getName())).andReturn(new ArrayList());
        siteService.assignParticipantCoordinators(createNamedInstance("Mayo Clinic", Site.class), expectedUser.getCsmUserId().toString());
        siteService.assignParticipantCoordinators(createNamedInstance("Northwestern Clinic", Site.class), expectedUser.getCsmUserId().toString());
        userDao.save(expectedUpdatedUser);

        replayMocks();

        User actual = service.getUserById(-100);
        actual.setName("updated");
        actual.addUserRole(userRole);
        service.saveUser(actual);
        verifyMocks();

        assertUserEquals(expectedUser, actual);
    }

    public void assertUserEquals(User expected, User actual) throws Exception{

        assertEquals("Names not equal", expected.getName(), actual.getName());
        assertEquals("Csm user ids not equal", expected.getCsmUserId(), actual.getCsmUserId());
        assertEquals("Active flags not equal", expected.getActiveFlag(), actual.getActiveFlag());
        assertEquals("Passwords not equal", expected.getPassword(), actual.getPassword());
        assertEquals("Different number of roles", expected.getUserRoles().size(), actual.getUserRoles().size());
//        for(UserRole userRole : expected.getUserRoles()) {
//
//            assertTrue("Expected and actual user roles are different", actual.getUserRoles().contains(userRole));
//        }
        Iterator<UserRole> expectedUserRolesIter = expected.getUserRoles().iterator();
        Iterator<UserRole> actualUserRolesIter = actual.getUserRoles().iterator();
        while (expectedUserRolesIter.hasNext()) {
            UserRole expectedUserRole = expectedUserRolesIter.next();
            UserRole actualUserRole = actualUserRolesIter.next();

            assertEquals("Role not equal", expectedUserRole.getRole(), actualUserRole.getRole());
            assertEquals("Different number of sites", expectedUserRole.getSites().size(), actualUserRole.getSites().size());
            for (Site expectedSite : expectedUserRole.getSites()) {
                assertTrue("Does not contain site", actualUserRole.getSites().contains(expectedSite));
            }
        }
    }

    public void testIsGroupEqualToRole() {
        UserService us = new UserService();
        assertTrue(us.isGroupEqualToRole(createCsmGroup(1L, "STUDY_COORDINATOR"), Role.STUDY_COORDINATOR));
        assertFalse(us.isGroupEqualToRole(createCsmGroup(1L, "STUDY_COORDINATOR"), Role.SITE_COORDINATOR));
    }

    public static GroupSearchCriteria eqCsmGroupSearchCriteria(GroupSearchCriteria group) {
        EasyMock.reportMatcher(new CsmGroupSearchCriteriaMatcher(group));
        return null;
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

            GroupSearchCriteria actual = (GroupSearchCriteria) object;

            if (expectedGroup.getMessage() != null ? !expectedGroup.getMessage().equals(actual.getMessage()) : actual.getMessage() != null)
                return false;

            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("Group with message =").append(expectedGroup.getMessage());
        }
    }

    public static <T> T unsortedAryEq(T t) {
        EasyMock.reportMatcher(new UnsortedArrayEquals(t));
        return null;
    }

    private static class UnsortedArrayEquals extends ArrayEquals {
        public UnsortedArrayEquals(Object expected) {
            super(expected);
            Arrays.sort((Object[])expected);
        }

        public boolean matches(Object actual) {
            Arrays.sort((Object[])actual);
            return super.matches(actual);
        }
    }

    private Group createCsmGroup(Long id, String name) {
        Group g = new Group();
        g.setGroupId(id);
        g.setGroupName(name);
        return g;
    }

    private class UserProvisioningManagerStub implements UserProvisioningManager {

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

        public void assignGroupsToUser(String string, String[] strings) throws CSTransactionException {}

        public List getObjects(SearchCriteria searchCriteria) {
            return allCsmGroups;
        }

        public void createUser(gov.nih.nci.security.authorization.domainobjects.User user) throws CSTransactionException {
            user.setUserId(100L);
        }

        public Application getApplicationById(String string) throws CSObjectNotFoundException {
            return new Application();
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

    }

}
