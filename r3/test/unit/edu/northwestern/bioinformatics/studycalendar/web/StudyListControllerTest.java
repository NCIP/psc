package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class StudyListControllerTest extends ControllerTestCase {
    private StudyListController controller;
    private StudyDao studyDao;
    private TemplateService templateService;
    private SiteService siteService;

    protected void setUp() throws Exception {
        super.setUp();
        controller = new StudyListController();
        studyDao = registerDaoMockFor(StudyDao.class);
        templateService = registerMockFor(TemplateService.class);
        siteService = registerMockFor(SiteService.class);
        controller.setStudyDao(studyDao);
        controller.setTemplateService(templateService);
        controller.setSiteService(siteService);
    }

    public void testModelAndView() throws Exception {
        List<Study> theList = new ArrayList<Study>();
        List<Site> sites = new ArrayList<Site>();
        ApplicationSecurityManager.setUser(request, "jimbo");

        expect(studyDao.getAll()).andReturn(theList);
        expect(templateService.checkOwnership("jimbo", theList)).andReturn(theList);
        expect(siteService.getSitesForSiteCd("jimbo")).andReturn(sites);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertSame("Studies list missing or wrong", theList, mv.getModel().get("studies"));
        assertSame("Sites list missing or wrong", sites, mv.getModel().get("sites"));
        assertEquals("studyList", mv.getViewName());
    }
}
