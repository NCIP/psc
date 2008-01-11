package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.resource.StreamRepresentation;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Rhett Sutphin
 */
public class TemplateResourceTest extends ResourceTestCase<TemplateResource> {
    private static final String STUDY_IDENT = "elf";
    private static final String MOCK_XML = "<study></study>";

    private StudyDao studyDao;
    private StudyXMLReader xmlReader;
    private StudyXMLWriter xmlWriter;
    private Study study;

    public void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        xmlReader = registerMockFor(StudyXMLReader.class);
        xmlWriter = registerMockFor(StudyXMLWriter.class);

        request.getAttributes().put(TemplateResource.STUDY_TEMPLATE_PARAMETER, TemplateResourceTest.STUDY_IDENT);
        study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(TemplateResourceTest.STUDY_IDENT, "Treatment")));
        Fixtures.assignIds(study);
    }

    protected TemplateResource createResource() {
        TemplateResource res = new TemplateResource();
        res.setStudyDao(studyDao);
        res.setStudyXMLReader(xmlReader);
        res.setStudyXMLWriter(xmlWriter);
        return res;
    }
    
    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("GET", "PUT");
    }

    public void testGetReturnsXml() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        expectStudyXmlized();

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
        expectStudyXmlized();
        expectReadXmlFromRequest();

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsStudyXml();
    }

    public void testPutNewXml() throws Exception {
        expect(studyDao.getStudyByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
        expectStudyXmlized();
        expectReadXmlFromRequest();

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsStudyXml();
    }

    private void expectReadXmlFromRequest() throws Exception {
        final InputStream inputStream = registerMockFor(InputStream.class);
        request.setEntity(new StreamRepresentation(MediaType.TEXT_XML) {
            public InputStream getStream() throws IOException {
                return inputStream;
            }

            public void write(OutputStream outputStream) throws IOException {
                throw new UnsupportedOperationException("write not implemented");
            }
        });

        expect(xmlReader.read(inputStream)).andReturn(study);
    }

    private void expectStudyXmlized() {
        expect(xmlWriter.createStudyXML(study)).andReturn(MOCK_XML);
    }

    private void assertResponseIsStudyXml() throws IOException {
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", MOCK_XML, actualEntityBody);
    }

}
