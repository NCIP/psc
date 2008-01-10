package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * @author Rhett Sutphin
 */
public class TemplateResourceTest extends StudyCalendarTestCase {
    private static final String STUDY_IDENT = "elf";

    private TemplateResource resource;
    private Request request;
    private Response response;

    private StudyDao studyDao;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);

        resource = new TemplateResource();
        resource.setStudyDao(studyDao);

        request = new Request();
        response = new Response(request);
        request.getAttributes().put(TemplateResource.STUDY_TEMPLATE_PARAMETER, STUDY_IDENT);
        study = setGridId("44", setId(44, createSingleEpochStudy(STUDY_IDENT, "Treatment")));
        assignIds(study);
    }

    public void testGetReturnsXml() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(study);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertContains("Entity does not appear to be proper XML for study", actualEntityBody, "assigned-identifier=\"elf\"");
        assertContains("Entity does not appear to be proper XML for study", actualEntityBody, "<planned-calendar");
    }

    public void testGet404sWhenStudyNotFound() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        doGet();

        assertEquals("Result should be not found", 404, response.getStatus().getCode());
    }

    private void doGet() {
        replayMocks();
        resource.init(null, request, response);
        resource.handleGet();
        verifyMocks();
    }
}
