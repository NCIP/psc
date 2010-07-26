package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class SelectStudySegmentControllerTest extends ControllerTestCase {
    private static final int STUDY_SEGMENT_ID = 90;

    private SelectStudySegmentController controller;
    private DeltaService deltaService;

    private StudySegment studySegment;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = Fixtures.createBasicTemplate();
        Fixtures.assignIds(study);
        studySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1);
        studySegment.setId(STUDY_SEGMENT_ID);

        StudySegmentDao studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        controller = new SelectStudySegmentController();
        deltaService = registerMockFor(DeltaService.class);

        controller.setStudySegmentDao(studySegmentDao);
        controller.setControllerTools(controllerTools);
        controller.setDeltaService(deltaService);
        controller.setTemplateService(new TestingTemplateService());
        controller.setApplicationSecurityManager(applicationSecurityManager);

        expect(studySegmentDao.getById(STUDY_SEGMENT_ID)).andStubReturn(studySegment);
        request.setParameter("studySegment", Integer.toString(STUDY_SEGMENT_ID));
        request.setMethod("GET");

        // basic test user can see anything
        setUserAndReturnMembership("jo", STUDY_CALENDAR_TEMPLATE_BUILDER).
            forAllStudies().forAllSites();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, valuesWithStudyAccess());
    }

    // TODO: test the inclusion of the plan tree hierarchy
    public void testRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/selectStudySegment", mv.getViewName());

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
        assertTrue("study segment is not wrapped", actualStudySegment instanceof StudySegmentTemplate);
        System.out.println("mv.getModel " + mv.getModel());
        assertEquals("Wrong model: " + mv.getModel(), 5, mv.getModel().size());
    }

    public void testRequestForDevelopment() throws Exception {
        request.setParameter("development", "true");
        study.setDevelopmentAmendment(new Amendment("dev"));
        expect(deltaService.revise(studySegment)).andReturn((StudySegment) studySegment.transientClone());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
        assertTrue("study segment is not wrapped", actualStudySegment instanceof StudySegmentTemplate);
        assertNotNull("dev revision missing", mv.getModel().get("developmentRevision"));
        assertTrue("Should be editable", (Boolean) mv.getModel().get("canEdit"));
    }

    public void testRequestForCurrentReleased() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
        assertTrue("study segment is not wrapped", actualStudySegment instanceof StudySegmentTemplate);
        assertNull("must not revise study", mv.getModel().get("developmentRevision"));
        assertFalse("Should not be editable", (Boolean) mv.getModel().get("canEdit"));
    }
}
