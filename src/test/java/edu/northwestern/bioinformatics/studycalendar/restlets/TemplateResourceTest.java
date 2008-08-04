package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.STUDY_IDENTIFIER;
import edu.northwestern.bioinformatics.studycalendar.service.ImportTemplateService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class TemplateResourceTest extends ResourceTestCase<TemplateResource> {
    private static final String STUDY_IDENT = "elf";

    private StudyDao studyDao;
    private Study study;
    private ImportTemplateService importTemplateService;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private StudyXmlSerializer studyXmlSerializer;
    private Date lastModifiedDate = DateUtils.createDate(2007, Calendar.OCTOBER, 5);


    public void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        importTemplateService = registerMockFor(ImportTemplateService.class);
        studyXmlSerializer = registerMockFor(StudyXmlSerializer.class);

        request.getAttributes().put(STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
        study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(STUDY_IDENT, "Treatment")));
        Fixtures.assignIds(study);
    }

    protected TemplateResource createResource() {
        TemplateResource templateResource = new TemplateResource();
        templateResource.setStudyDao(studyDao);
        templateResource.setXmlSerializer(studyXmlSerializer);
        templateResource.setImportTemplateService(importTemplateService);
        return templateResource;
    }

    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("GET", "PUT");
    }

    public void testGetReturnsXml() throws Exception {

        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        expectObjectXmlized(study);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();

        assertEquals("modification date can not be null", lastModifiedDate, response.getEntity().getModificationDate());

    }

    public void testGet404sWhenStudyNotFound() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        doGet();

        assertEquals("Result should be not found", 404, response.getStatus().getCode());
    }

    public void testPutExistingXml() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);

        expectReadXmlFromRequestAs(study);
        expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);

        expect(importTemplateService.readAndSaveTemplate(study, null)).andReturn(study);

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        expectReadXmlFromRequestAs(study);
        expect(importTemplateService.readAndSaveTemplate(null, null)).andReturn(study);
        expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);

        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();

    }

    protected void expectReadXmlFromRequestAs(Study expectedRead) throws Exception {
        final InputStream in = registerMockFor(InputStream.class);
        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expect(studyXmlSerializer.readDocument(in)).andReturn(expectedRead);
    }


    @SuppressWarnings({"unchecked"})
    protected void expectObjectXmlized(Object o) {
        expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);
        expect(studyXmlSerializer.readLastModifiedDate(isA(InputStream.class))).andReturn(lastModifiedDate);

    }

}
