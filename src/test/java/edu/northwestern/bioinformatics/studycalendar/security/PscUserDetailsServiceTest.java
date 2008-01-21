package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.acegisecurity.userdetails.UserDetails;
import static org.easymock.EasyMock.expect;

public class PscUserDetailsServiceTest extends StudyCalendarTestCase {
    private User user;
    private UserDao userDao;
    private PscUserDetailsService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);

        service = new PscUserDetailsService();
        service.setUserDao(userDao);

        user = Fixtures.createUser(1, "John", 1L, true);
    }

    public void testLoadUserGivesActualPscUser() throws Exception {
        expect(userDao.getByName(user.getName())).andReturn(user);
        replayMocks();

        UserDetails actualUserDetails = service.loadUserByUsername(user.getName());
        verifyMocks();

        assertSame(user, actualUserDetails);
    }
}
