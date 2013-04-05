/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendment;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static java.util.Calendar.JANUARY;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewControllerTest extends ControllerTestCase {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private DeltaService deltaService;
    private Study study;
    private Amendment amendment;
    private SchedulePreviewController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller =  new SchedulePreviewController();
        deltaService = registerMockFor(DeltaService.class);
        studyDao =  registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);

        controller.setAmendmentDao(amendmentDao);
        controller.setStudyDao(studyDao);
        controller.setDeltaService(deltaService);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations,
                DATA_IMPORTER,
                STUDY_QA_MANAGER, STUDY_TEAM_ADMINISTRATOR,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_CREATOR,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER);
    }

    public void testModelAndView() throws Exception {
        request.addParameter("study","11");
        request.addParameter("amendment", "12");
        study= setId(10, Fixtures.createBasicTemplate("study"));
        amendment =  createAmendment("testAmendment", DateTools.createDate(2004, JANUARY, 4));
        study.setDevelopmentAmendment(new Amendment());
        expect(studyDao.getById(11)).andReturn(study);
        expect(amendmentDao.getById(12)).andReturn(amendment);
        replayMocks();
        ModelAndView actual = controller.handleRequestInternal(request, response);
        verifyMocks();

        assertTrue("Missing model object", actual.getModel().containsKey("amendmentIdentifier"));
        assertEquals("Wrong values for key", "current", actual.getModel().get("amendmentIdentifier"));
        assertEquals("Wrong values for key", study, actual.getModel().get("study"));
        assertEquals("Wrong values for key", true, actual.getModel().get("schedulePreview"));
        assertEquals("Wrong view", "subject/schedule", actual.getViewName());
    }
}
