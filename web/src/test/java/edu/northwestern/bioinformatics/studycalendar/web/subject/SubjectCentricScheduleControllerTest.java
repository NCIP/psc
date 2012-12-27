/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.NowFactory;

import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static org.easymock.EasyMock.expect;

public class SubjectCentricScheduleControllerTest extends ControllerTestCase {
    private SubjectCentricScheduleController controller;
    private SubjectDao subjectDao;
    private NowFactory nowFactory;
    private Subject subject;
    private ApplicationSecurityManager applicationSecurityManagerMock;
    private PscUser sscm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        nowFactory = registerMockFor(NowFactory.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        applicationSecurityManagerMock = registerMockFor(ApplicationSecurityManager.class);

        controller = new SubjectCentricScheduleController();
        controller.setSubjectDao(subjectDao);
        controller.setNowFactory(nowFactory);
        controller.setApplicationSecurityManager(applicationSecurityManagerMock);

        StudySite ss = createStudySite(
            createNamedInstance("T1", Study.class),
            createSite("NU")
        );

        subject = Fixtures.createSubject("Jo", "Blo");

        StudySubjectAssignment assignment = createAssignment(ss, subject);

        assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(DateTools.createDate(2010, 2, 15), 1)
        );

        sscm = (new PscUserBuilder().add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().toUser());

    }

    public void testCanUpdateScheduleIsTrue() throws Exception {
        Map<String, Object> actualModel = handleRequest();
        assertTrue((Boolean) actualModel.get("canUpdateSchedule"));
    }

    public void testCanUpdateScheduleIsFalse() throws Exception {
        sscm.getMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).notForAllSites().notForAllStudies();

        Map<String, Object> actualModel = handleRequest();
        assertFalse((Boolean) actualModel.get("canUpdateSchedule"));
    }

    private Map<String, Object> handleRequest() throws Exception {
        request.addParameter("subject", "SUBJECT-GRID-ID-0");

        expect(subjectDao.getByGridId("SUBJECT-GRID-ID-0")).andReturn(subject);
        expect(applicationSecurityManagerMock.getUser()).andReturn(sscm);

        replayMocks();
        Map<String, Object> actualModel = controller.handleRequest(request, response).getModel();
        verifyMocks();
        return actualModel;
    }
}
