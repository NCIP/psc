/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodControllerTest extends ControllerTestCase {
    private NewPeriodController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        StudySegmentDao studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        PeriodDao periodDao = registerDaoMockFor(PeriodDao.class);

        Study study = createSingleEpochStudy("S", "E", "Ss");
        StudySegment seg = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);

        request.setParameter("studySegment", "4");
        expect(studySegmentDao.getById(4)).andStubReturn(seg);

        controller = new NewPeriodController();
        controller.setControllerTools(controllerTools);
        controller.setStudySegmentDao(studySegmentDao);
        controller.setPeriodDao(periodDao);
        controller.setTemplateService(templateService);
    }

    public void testAuthorizedRoles() throws Exception {
        replayMocks();
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CALENDAR_TEMPLATE_BUILDER);
    }    

    public void testCommandForRegularNewPeriod() throws Exception {
        Object actual = controller.formBackingObject(request);
        assertTrue(actual instanceof NewPeriodCommand);
    }

    public void testCommandForCopyNewPeriod() throws Exception {
        request.setParameter("selectedPeriod", "7");
        Object actual = controller.formBackingObject(request);
        assertTrue(actual instanceof CopyPeriodCommand);
    }
}
