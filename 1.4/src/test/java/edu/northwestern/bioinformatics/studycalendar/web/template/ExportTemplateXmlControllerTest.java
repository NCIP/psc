package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import static org.easymock.classextension.EasyMock.*;

import java.io.UnsupportedEncodingException;

/**
 * @author Rhett Sutphin
 */
public class ExportTemplateXmlControllerTest extends ControllerTestCase {
    private static final String STUDY_IDENT = "ABC 1234";
    private ExportTemplateXmlController controller;
    private StudyDao studyDao;

    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // TODO: check that this is the right property
        request.setPathInfo(String.format("/template/display/%s.xml", STUDY_IDENT));

        study = new Study();
        study.setAssignedIdentifier(STUDY_IDENT);
        study.setGridId(STUDY_IDENT);
        study.setPlannedCalendar(new PlannedCalendar());
        study.getPlannedCalendar().setGridId("63");

        studyDao = registerDaoMockFor(StudyDao.class);

        controller = new ExportTemplateXmlController();
        controller.setStudyDao(studyDao);
    }

    public void testTreatIdentAsGridIdFirst() throws Exception {
        expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(study);
        handle(200);
        assertXmlResponse();
    }

    public void testTreatIdentAsAssignedIdentifierSecond() throws Exception {
        expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(null);
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        handle(200);
        assertXmlResponse();
    }

    private void assertXmlResponse() throws UnsupportedEncodingException {
        assertEquals("text/xml", response.getContentType());
        String content = response.getContentAsString();
        System.out.println(content);
        assertContains(content, "<study");
        assertContains(content, String.format("assigned-identifier=\"%s\"", STUDY_IDENT));
    }

    public void testHttp400ForMissingIdentifier() throws Exception {
        request.setPathInfo("/etc/etc/.xml");
        handle(400);
    }
    
    public void testHttp400ForNonMatchingUri() throws Exception {
        request.setPathInfo("/etc/etc/ABC 1234.doc");
        handle(400);
    }

    public void testHttp404ForUnresolvableIdentifier() throws Exception {
        expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(null);
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        handle(404);
    }

    private void handle(int expectedResponseStatus) throws Exception {
        replayMocks();
        assertNull("No model and view should ever be returned", controller.handleRequest(request, response));
        assertEquals("Wrong response status", expectedResponseStatus, response.getStatus());
        verifyMocks();
    }
}
