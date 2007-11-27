package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import gov.nih.nci.cabig.ctms.audit.DataAuditInfo;

/**
 * @author Rhett Sutphin
 */
public class StudyListControllerTest extends ControllerTestCase {
    private StudyListController controller;
    private StudyDao studyDao;
    private UserDao userDao;
    private TemplateService templateService;
    private SiteService siteService;

    protected void setUp() throws Exception {
        super.setUp();
        controller = new StudyListController();
        studyDao = registerDaoMockFor(StudyDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        templateService = registerMockFor(TemplateService.class);
        siteService = registerMockFor(SiteService.class);
        controller.setStudyDao(studyDao);
        controller.setUserDao(userDao);
        controller.setTemplateService(templateService);
        controller.setSiteService(siteService);
    }

    public void testModelAndView() throws Exception {
        Study complete = Fixtures.createSingleEpochStudy("Complete", "E1");
        complete.setAmendment(new Amendment());
        Study incomplete = Fixtures.createSingleEpochStudy("Incomplete", "E1");
        incomplete.setAmendment(null);
        incomplete.setDevelopmentAmendment(new Amendment());
        Study both = Fixtures.createSingleEpochStudy("Available but amending", "E1");
        both.setDevelopmentAmendment(new Amendment());
        both.setAmendment(new Amendment());
        List<Study> studies = Arrays.asList(incomplete, complete, both);
        List<Site> sites = new ArrayList<Site>();
        SecurityContextHolderTestHelper.setSecurityContext("jimbo", "password");

        expect(studyDao.getAll()).andReturn(studies);
        User user = Fixtures.createUser("jimbo", Role.SUBJECT_COORDINATOR);
        expect(userDao.getByName("jimbo")).andReturn(user);
        expect(templateService.filterForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(studies);
        expect(siteService.getSitesForSiteCd("jimbo")).andReturn(sites);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Complete studies list missing or wrong", Arrays.asList(complete, both),
            mv.getModel().get("assignableStudies"));
        assertEquals("Incomplete studies list missing or wrong", Arrays.asList(incomplete, both),
            mv.getModel().get("inDevelopmentStudies"));
        assertSame("Sites list missing or wrong", sites, mv.getModel().get("sites"));
        assertEquals("studyList", mv.getViewName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DataAuditInfo.setLocal(null);
        ApplicationSecurityManager.removeUserSession();
    }

}
