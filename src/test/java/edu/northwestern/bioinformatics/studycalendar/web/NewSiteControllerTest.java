package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import org.springframework.orm.hibernate3.HibernateTemplate;
import static org.easymock.EasyMock.expect;


import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class NewSiteControllerTest  extends ControllerTestCase {
    private NewSiteController controller = new NewSiteController();
    private SiteDao siteDao = new SiteDao();
    private SiteService siteService = new SiteService();
    private NewSiteCommand command;
    private StudyCalendarAuthorizationManager authorizationManager;
    private Site nu;
    private HibernateTemplate hibernateTemplate;

    protected void setUp() throws Exception {
        super.setUp();
        nu = setId(1, Fixtures.createNamedInstance("Northwestern", Site.class));
        siteService.setSiteDao(siteDao);
        hibernateTemplate = registerMockFor(HibernateTemplate.class);
        siteDao.setHibernateTemplate(hibernateTemplate);
        command = new NewSiteCommand(nu, siteService);
        siteDao = registerMockFor(SiteDao.class);
        controller.setSiteDao(siteDao);
        controller.setSiteService(siteService);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);
        siteService.setStudyCalendarAuthorizationManager(authorizationManager);
    }

    public void testReferenceData() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request,command,null);
        assertEquals("Comamnd not Match","Create / Edit",refdata.get("action"));
    }

    public void testFormView() throws Exception {
        assertEquals("Form view does not exist","createSite", controller.getFormView());
    }

    public void testOnSubmit() throws Exception {
        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(null);
        authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        hibernateTemplate.saveOrUpdate(nu);
        replayMocks();
        Site siteCreated = command.createSite();
        verifyMocks();
        assertNotNull("site not created", siteCreated);
    }

}
