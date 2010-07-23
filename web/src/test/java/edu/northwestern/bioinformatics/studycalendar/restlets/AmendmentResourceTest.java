package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.AmendmentXmlSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.nwu.bioinformatics.commons.DateUtils.*;
import static org.easymock.EasyMock.*;

/**
 * @author Saurabh Agrawal
 */
public class AmendmentResourceTest extends AuthorizedResourceTestCase<AmendmentResource> {
    public static final String SOURCE_NAME = "Mutant Study";
    public static final String SOURCE_NAME_ENCODED = "Mutant%20Study";
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String AMENDMENT_KEY = "2007-10-19~Amendment B";
    private static final String AMENDMENT_KEY_ENCODED = "2007-10-19~Amendment%20B";
    private static final String CURRENT_AMENDMENT_KEY = "current";

    private Study study;
    private Amendment amendment, developmentAmendment;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private StudyService studyService;
    private BeanFactory beanFactory;

    private AmendmentXmlSerializer amendmentXmlSerializer;
    private TemplateDevelopmentService templateDevService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        beanFactory = registerMockFor(BeanFactory.class);

        studyService = registerMockFor(StudyService.class);
        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), SOURCE_NAME_ENCODED);
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AMENDMENT_KEY_ENCODED);

        PlannedCalendar calendar = new PlannedCalendar();

        amendment = new Amendment();
        amendment.setName("Amendment B");
        amendment.setDate(createDate(2007, Calendar.OCTOBER, 19));
        amendment.setReleasedDate(createDate(2007, Calendar.OCTOBER, 19));

        developmentAmendment = new Amendment();
        developmentAmendment.setName("Amendment C");
        developmentAmendment.setDate(createDate(2007, Calendar.OCTOBER, 18));

        study = createNamedInstance(SOURCE_NAME, Study.class);
        study.setPlannedCalendar(calendar);
        study.pushAmendment(amendment);
        study.setDevelopmentAmendment(developmentAmendment);

        amendmentXmlSerializer = new AmendmentXmlSerializer();

        templateDevService = registerMockFor(TemplateDevelopmentService.class);
     }


    @Override
    @SuppressWarnings({"unchecked"})
    protected AmendmentResource createAuthorizedResource() {
        AmendmentResource resource = new AmendmentResource();

        resource.setStudyDao(studyDao);
        resource.setAmendmentDao(amendmentDao);
        resource.setXmlSerializer(xmlSerializer);
        resource.setStudyService(studyService);
        resource.setBeanFactory(beanFactory);
        resource.setTemplateDevelopmentService(templateDevService);
        return resource;
    }

    public void testPutExistingAmendment() throws Exception {
        expectFoundStudy();
        expectFoundAmendment();

        String expectedXml = "<amendment xmlns=\"http://bioinformatics.northwestern.edu/ns/psc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"Amendment B\" date=\"2007-10-19\" mandatory=\"true\" " +
                "xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd\"></amendment>";

        final InputStream in = new ByteArrayInputStream(expectedXml.getBytes());

        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expectAmendmentXmlSerializer();
        expectAmendmentXmlSerializer();
        templateDevService.deleteDevelopmentAmendmentOnly(study);
        studyService.save(study);
        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
    }

    public void testPutNewAmendment() throws Exception {
        study.setDevelopmentAmendment(null);
        expectFoundStudy();
        expectFoundAmendment();

        String expectedXml = "<amendment xmlns=\"http://bioinformatics.northwestern.edu/ns/psc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"Amendment B\" date=\"2007-10-19\" mandatory=\"true\" " +
                "xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd\"></amendment>";

        final InputStream in = new ByteArrayInputStream(expectedXml.getBytes());

        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expectAmendmentXmlSerializer();
        expectAmendmentXmlSerializer();

        studyService.save(study);
        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET", "DELETE");
    }

    public void testGetForAmendment() throws Exception {
        expectFoundStudy();
        expectFoundAmendment();
        expectAmendmentXmlSerializer();
        expectAmendmentXmlSerializer();
        doGet();

        String exectedEntityBody = amendmentXmlSerializer.createDocumentString(amendment);
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", exectedEntityBody, actualEntityBody);

        assertResponseStatus(Status.SUCCESS_OK);
        // TODO: modification date handling for this resource is broken, but I can't fix it right now -- RMS20080904
        // assertNotNull("modification date must not be null", response.getEntity().getModificationDate());
    }

    public void testGetForDevelopmentAmendment() throws Exception {
        expectFoundStudy();
        expect(amendmentDao.getByNaturalKey(AMENDMENT_KEY, study)).andReturn(developmentAmendment);
        expectAmendmentXmlSerializer();
        expectAmendmentXmlSerializer();

        doGet();
        String exectedEntityBody = amendmentXmlSerializer.createDocumentString(developmentAmendment);
        String actualEntityBody = response.getEntity().getText();
        log.debug("Expected:\n{}", exectedEntityBody);
        log.debug("Actual:\n{}", actualEntityBody);

        assertEquals("Wrong text", exectedEntityBody, actualEntityBody);

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithCurrentAmendmentIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), CURRENT_AMENDMENT_KEY);

        expectFoundStudy();
        expectAmendmentXmlSerializer();
        expectAmendmentXmlSerializer();

        doGet();

        String exectedEntityBody = amendmentXmlSerializer.createDocumentString(amendment);
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", exectedEntityBody, actualEntityBody);


        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithNoStudyIdentifier() {
        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), "");
        expectStudyNotFound();

        doGet();
        assertEquals("Result should be 404", Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testGetWithNoAssignmentIdentifier() {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), "");
        expectAmendmentNotFound();

        expectFoundStudy();

        doGet();
        assertEquals("Result should be 404", Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testDeleteAmendmentWhichIsAReleasedAmendment() throws Exception {
        expectFoundStudy();
        expectFoundAmendment();
        doDelete();

        assertEquals("Result should be 404", Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
    }

    public void testDeleteAmendmentWhichIsDoesNotApplyToStudy() throws Exception {
        expectFoundStudy();
        expect(amendmentDao.getByNaturalKey(AMENDMENT_KEY, study)).andReturn(null);
        doDelete();

        assertEquals("Result should be 404", Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
    }

    public void testDeleteAmendmentWhichIsNull() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), "");
        expectFoundStudy();
        expectAmendmentNotFound();
        doDelete();

        assertEquals("Result should be 404", Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
    }

    public void testDeleteDevelopmentAmendment() throws Exception {
        expectFoundStudy();
        expect(amendmentDao.getByNaturalKey(AMENDMENT_KEY, study)).andReturn(developmentAmendment);
        templateDevService.deleteDevelopmentAmendmentOnly(study);
        doDelete();

        assertEquals("Result should be 200", Status.SUCCESS_OK, response.getStatus());
    }

    ////// HELPERS

    private void expectFoundStudy() {
        expect(studyDao.getByAssignedIdentifier(SOURCE_NAME)).andReturn(study);
    }

    private void expectAmendmentXmlSerializer() {
        expect(beanFactory.getBean("amendmentXmlSerializer")).andReturn(amendmentXmlSerializer);
    }

    private void expectFoundAmendment() {
        expect(amendmentDao.getByNaturalKey(AMENDMENT_KEY, study)).andReturn(amendment);
    }

    private void expectStudyNotFound() {
        expect(studyDao.getByAssignedIdentifier("")).andReturn(null);
    }

    private void expectAmendmentNotFound() {
        expect(amendmentDao.getByNaturalKey("", study)).andReturn(null);
    }
}
