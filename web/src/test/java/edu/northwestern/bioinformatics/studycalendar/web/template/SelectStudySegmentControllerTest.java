/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.*;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Collections;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.amend;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper.setUserAndReturnMembership;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta.createDeltaFor;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.valuesWithStudyAccess;
import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class SelectStudySegmentControllerTest extends ControllerTestCase {
    private static final int STUDY_SEGMENT_ID = 90;

    private SelectStudySegmentController controller;
    private DeltaService deltaService;

    private StudySegment studySegment;
    private Study study;
    private AmendmentService amendmentService;
    private AmendmentDao amendmentDao;
    private WorkflowService workflowService;

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
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        templateService = registerMockFor(TemplateService.class);
        workflowService = registerMockFor(WorkflowService.class);

        amendmentService = new AmendmentService();
        amendmentService.setTemplateService(new TestingTemplateService());

        controller.setStudySegmentDao(studySegmentDao);
        controller.setControllerTools(controllerTools);
        controller.setDeltaService(deltaService);
        controller.setAmendmentDao(amendmentDao);
        controller.setAmendmentService(amendmentService);
        controller.setWorkflowService(workflowService);
        controller.setTemplateService(new TestingTemplateService());
        controller.setApplicationSecurityManager(applicationSecurityManager);

        expect(studySegmentDao.getById(STUDY_SEGMENT_ID)).andStubReturn(studySegment);
        StudyWorkflowStatus status = registerMockFor(StudyWorkflowStatus.class);
        expect(workflowService.build((Study) notNull(), (PscUser) notNull())).andReturn(status);
        expect(status.getMessages()).andReturn(Collections.EMPTY_LIST);
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
        assertEquals("Wrong model: " + mv.getModel(), 6, mv.getModel().size());
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

    public void testRequestForPreviousVersion() throws Exception {
        int AMENDMENT_ID = -20;

        request.setParameter("amendment", Integer.toString(AMENDMENT_ID));

        Amendment second = new Amendment("second");
        PlannedCalendar cal = study.getPlannedCalendar();
        second.addDelta(createDeltaFor(cal, Remove.create(cal.getEpochs().get(0))));
        second.addDelta(createDeltaFor(cal, Remove.create(cal.getEpochs().get(1))));
        Study amended = study.transientClone();
        amended.setDevelopmentAmendment(second);
        amend(amended);

        TemplateService templateService = registerMockFor(TemplateService.class);
        controller.setTemplateService(templateService);

        expect(amendmentDao.getById(AMENDMENT_ID)).andReturn(amended.getAmendment().getPreviousAmendment());
        expect(templateService.findStudy(studySegment)).andReturn(study);
        
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object actualStudySegment = mv.getModel().get("studySegment");
        assertNotNull("study segment missing", actualStudySegment);
    }
}
