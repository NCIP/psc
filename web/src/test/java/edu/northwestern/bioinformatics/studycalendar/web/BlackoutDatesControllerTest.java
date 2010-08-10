package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;
import static org.easymock.EasyMock.expect;

public class BlackoutDatesControllerTest extends ControllerTestCase {
    private Site nu;
    private BlackoutDatesController controller;
    private SiteDao siteDao;

    protected void setUp() throws Exception {
        super.setUp();
        nu = setId(-99, createNamedInstance("Northwestern", Site.class));
        siteDao = registerDaoMockFor(SiteDao.class);
        controller = new BlackoutDatesController();
        controller.setSiteDao(siteDao);
    }

    public void testAuthorizedRoles() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        String[] siteId = {nu.getId().toString()};
        params.put("site", siteId);
        expect(siteDao.getById(nu.getId())).andReturn(nu);

        replayMocks();

        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, params);
        assertRolesAllowed(actualAuthorizations, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        assertSiteScopedRolesAllowed(actualAuthorizations, nu, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void testAuthorizedRolesWithMissingId() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        try {
            controller.authorizations(null, params);
            fail("Exception should be thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Wrong exception message", "Site parameter is invalid", e.getMessage());
        }

    }
}
