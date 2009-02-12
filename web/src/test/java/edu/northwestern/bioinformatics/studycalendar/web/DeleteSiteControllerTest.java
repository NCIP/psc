package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class DeleteSiteControllerTest extends ControllerTestCase {
    private DeleteSiteController controller = new DeleteSiteController();
    private SiteService siteService;
    private SiteDao siteDao = new SiteDao();

    protected void setUp() throws Exception {
         super.setUp();
         siteService = registerMockFor(SiteService.class);
         siteDao = registerDaoMockFor(SiteDao.class);
         controller.setSiteService(siteService);
         controller.setSiteDao(siteDao);
     }

    public void testDeleteSite() throws Exception {
        request.setParameter("site", "1");
        Site site = setId(1, createNamedInstance("Northwestern", Site.class));
        expect(siteDao.getById(1)).andReturn(site);
        siteService.removeSite(site);
        replayMocks();
        controller.handleRequestInternal(request, response);
        verifyMocks();
    }


}
