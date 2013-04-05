/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.admin.PurgeStudyController;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

public class PurgeStudyControllerTest extends ControllerTestCase {
    private PurgeStudyController controller;
    private StudyDao studyDao;
    private StudyService studyService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        studyService = registerMockFor(StudyService.class);

        controller = new PurgeStudyController();
        controller.setStudyDao(studyDao);
        controller.setStudyService(studyService);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_QA_MANAGER);
    }

    public void testSubmitWithNoAssignedIdentifier() throws Exception {
        request.setMethod("POST");
        ModelAndView mv = controller.handleRequest(request, response);
        String actual = (String) mv.getModel().get("status");
        assertEquals("Wrong status", "Please select a study", actual);
    }

    public void testSubmitWithNoStudyFound() throws Exception {
        request.setMethod("POST");
        request.setParameter("studyAssignedIdentifier", "NCI-999");

        expect(studyDao.getByAssignedIdentifier("NCI-999")).andReturn(null);

        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        String actual = (String) mv.getModel().get("status");
        assertEquals("Wrong status", "Study NCI-999 failed to be purged because it could not be found", actual);
    }

    public void testSubmitSuccess() throws Exception {
        request.setMethod("POST");
        request.setParameter("studyAssignedIdentifier", "NCI-999");

        Study study = createNamedInstance("NCI-999", Study.class);
        expect(studyDao.getByAssignedIdentifier("NCI-999")).andReturn(study);
        studyService.purge(study);

        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        
        String actual = (String) mv.getModel().get("status");
        assertEquals("Wrong status", "Study NCI-999 has been successfully purged", actual);
    }
}
