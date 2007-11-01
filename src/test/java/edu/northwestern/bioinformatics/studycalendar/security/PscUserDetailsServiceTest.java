package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import static org.easymock.EasyMock.expect;
import org.acegisecurity.userdetails.UserDetails;

import java.util.Collections;
import java.util.Set;

public class PscUserDetailsServiceTest extends StudyCalendarTestCase {
    private User user;
    private UserDao userDao;
    private UserProvisioningManager userprovisioningManager;
    private Set<Group> groups;
    private PscUserDetailsService service;
    private gov.nih.nci.security.authorization.domainobjects.User csmUser;

    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        userprovisioningManager = registerMockFor(UserProvisioningManager.class);

        service = new PscUserDetailsService();
        service.setUserDao(userDao);
        service.setCsmUserProvisioningManager(userprovisioningManager);
        service.setRolePrefix("");

        user = Fixtures.createUser(1, "John", 1L, true);

        csmUser = new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setUserId(user.getCsmUserId());
        csmUser.setLoginName(user.getName());

        Group group = new Group();
        group.setGroupName(Role.PARTICIPANT_COORDINATOR.csmGroup());
        groups = Collections.singleton(group);
    }

    public void testLoadUserByNameEnabledUser() throws Exception {
        expect(userprovisioningManager.getUser(csmUser.getName())).andReturn(csmUser);
        expect(userprovisioningManager.getGroups(csmUser.getUserId().toString())).andReturn(groups);
        expect(userDao.getByName(user.getName())).andReturn(user);
        replayMocks();

        UserDetails actualUserDetails = service.loadUserByUsername(user.getName());
        verifyMocks();

        assertEquals("Wrong number of groups", 1, actualUserDetails.getAuthorities().length);
        assertTrue("Wrong enabled value", actualUserDetails.isEnabled());
        assertTrue("Wrong enabled value", actualUserDetails.isAccountNonLocked());
        assertTrue("Wrong enabled value", actualUserDetails.isAccountNonExpired());
        assertTrue("Wrong enabled value", actualUserDetails.isAccountNonExpired());
    }

    public void testLoadUserByNameDisabledUser() throws Exception {
        user.setActiveFlag(false);
        expect(userprovisioningManager.getUser(csmUser.getName())).andReturn(csmUser);
        expect(userprovisioningManager.getGroups(csmUser.getUserId().toString())).andReturn(groups);
        expect(userDao.getByName(user.getName())).andReturn(user);
        replayMocks();

        UserDetails actualUserDetails = service.loadUserByUsername(user.getName());
        verifyMocks();

        assertEquals("Wrong number of groups", 1, actualUserDetails.getAuthorities().length);
        assertFalse("Wrong enabled value", actualUserDetails.isEnabled());
        assertFalse("Wrong enabled value", actualUserDetails.isAccountNonLocked());
        assertTrue("Wrong enabled value", actualUserDetails.isAccountNonExpired());
        assertTrue("Wrong enabled value", actualUserDetails.isAccountNonExpired());
    }
}
