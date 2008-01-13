package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlFactory;
import static org.easymock.classextension.EasyMock.*;
import org.restlet.data.MediaType;
import org.restlet.resource.ReaderRepresentation;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Rhett Sutphin
 */
public class TemplateResourceTest extends ResourceTestCase<TemplateResource> {
    private static final String STUDY_IDENT = "elf";
    private static final String MOCK_XML = "<study></study>";

    private StudyDao studyDao;
    private StudyCalendarXmlFactory xmlFactory;
    private Study study;

    public void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        xmlFactory = registerMockFor(StudyCalendarXmlFactory.class);

        request.getAttributes().put(STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
        study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(STUDY_IDENT, "Treatment")));
        Fixtures.assignIds(study);
    }

    protected TemplateResource createResource() {
        TemplateResource res = new TemplateResource();
        res.setStudyDao(studyDao);
        res.setStudyCalendarXmlFactory(xmlFactory);
        return res;
    }
    
    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("GET", "PUT");
    }

    public void testGetReturnsXml() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        expectStudyXmlized(study);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsStudyXml();
    }

    public void testGet404sWhenStudyNotFound() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        doGet();

        assertEquals("Result should be not found", 404, response.getStatus().getCode());
    }

    public void testPutExistingXml() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        expectStudyXmlized(study);
        expectReadXmlFromRequest();

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsStudyXml();
    }

    public void testPutNewXml() throws Exception {
        Study newStudy = new Study();
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(newStudy);
        expectStudyXmlized(newStudy);
        expectReadXmlFromRequest();

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsStudyXml();
    }

    private void expectReadXmlFromRequest() throws Exception {
        final Reader reader = registerMockFor(Reader.class);
        request.setEntity(new ReaderRepresentation(reader, MediaType.TEXT_XML));

        expect(xmlFactory.readDocument(reader)).andReturn(study);
    }

    private void expectStudyXmlized(Study s) {
        expect(xmlFactory.createDocumentString(s)).andReturn(MOCK_XML);
    }

    private void assertResponseIsStudyXml() throws IOException {
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", MOCK_XML, actualEntityBody);
    }
}
