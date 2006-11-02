package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor.REQUESTED_URL_ATTRIBUTE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.nih.nci.security.exceptions.CSException;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.web.ParticipantCoordinatorController;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class ParticipantCoordinatorControllerTest extends ControllerTestCase {
    private ParticipantCoordinatorController controller;
    private SiteService siteService;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        
        siteService = registerMockFor(SiteService.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        controller = new ParticipantCoordinatorController();
        controller.setSiteService(siteService);
        controller.setStudyDao(studyDao);
    }
    
    public void testRefData() throws Exception {
    	List<Site> sites = new ArrayList<Site>();
    	Study study = new Study();
    	request.addParameter("id", "-1");
    	ApplicationSecurityManager.setUser(request, "sc1");
    	expect(siteService.getSitesForSiteCd("sc1")).andReturn(sites);
    	expect(studyDao.getById(-1)).andReturn(study);
    	replayMocks();
    	
    	Map map = controller.referenceData(request);
    	
    	verifyMocks();
    	assertSame("Sites not the same.", sites, map.get("sites"));
    	assertSame("Study not available.", study, map.get("study"));
    }
    

    
}
