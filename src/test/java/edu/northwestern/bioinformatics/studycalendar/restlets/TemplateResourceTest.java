package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.STUDY_IDENTIFIER;
import edu.northwestern.bioinformatics.studycalendar.service.ImportTemplateService;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;

import java.io.InputStream;

/**
 * @author Rhett Sutphin
 */
public class TemplateResourceTest extends ResourceTestCase<TemplateResource> {
    private static final String STUDY_IDENT = "elf";

    private StudyDao studyDao;
    private Study study;
    private ImportTemplateService importTemplateService;

    public void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        importTemplateService = registerMockFor(ImportTemplateService.class);

        request.getAttributes().put(STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
        study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(STUDY_IDENT, "Treatment")));
        Fixtures.assignIds(study);
    }

    protected TemplateResource createResource() {
        TemplateResource templateResource = new TemplateResource();
        templateResource.setStudyDao(studyDao);
        templateResource.setXmlSerializer(xmlSerializer);
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
    }

    public void testGet404sWhenStudyNotFound() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        doGet();

        assertEquals("Result should be not found", 404, response.getStatus().getCode());
    }

    public void testPutExistingXml() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        
        final InputStream in = registerMockFor(InputStream.class);
        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expect(importTemplateService.readAndSaveTemplate(eq(study), (InputStream) notNull())).andReturn(study);
        expectObjectXmlized(study);

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);

        final InputStream in = registerMockFor(InputStream.class);
        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expect(importTemplateService.readAndSaveTemplate((Study) isNull(), (InputStream) notNull())).andReturn(study);
        expectObjectXmlized(study);

        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

}
