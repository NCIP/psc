package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.expect;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.web.ParticipantCoordinatorController;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class ParticipantCoordinatorControllerTest extends ControllerTestCase {
    private ParticipantCoordinatorController controller;
    private TemplateService templateService;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        
        templateService = registerMockFor(TemplateService.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        controller = new ParticipantCoordinatorController();
        controller.setTemplateService(templateService);
        controller.setStudyDao(studyDao);

        SecurityContextHolderTestHelper.setSecurityContext("sc1", "pass");
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession(request);
    }

    public void testRefData() throws Exception {
        List<Site> sites = new ArrayList<Site>();
        Study study = new Study();
        request.addParameter("id", "-1");
        expect(templateService.getSitesForTemplateSiteCd("sc1", study)).andReturn(sites);
        expect(studyDao.getById(-1)).andReturn(study);
        replayMocks();

        Map map = controller.referenceData(request);

        verifyMocks();
        assertSame("Sites not the same.", sites, map.get("sites"));
        assertSame("Study not available.", study, map.get("study"));
    }
}
