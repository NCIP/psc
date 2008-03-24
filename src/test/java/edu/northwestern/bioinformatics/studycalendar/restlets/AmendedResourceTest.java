package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.Calendar;

/**
 * @author Saurabh Agrawal
 */
public class AmendedResourceTest extends AuthorizedResourceTestCase<AmendedResource> {
    public static final String SOURCE_NAME = "Mutant Study";
    public static final String SOURCE_NAME_ENCODED = "Mutant%20Study";

    private static final String AMENDMENT_KEY = "2007-10-19~Amendment B";
    private static final String AMENDMENT_KEY_ENCODED = "2007-10-19~Amendment%20B";
    private static final String CURRENT_AMENDMENT_KEY = "current";

    private Study study;
    private Amendment amendment;
    private PlannedCalendar calendar;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), SOURCE_NAME_ENCODED);
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AMENDMENT_KEY_ENCODED);

        calendar = new PlannedCalendar();

        amendment = new Amendment();
        amendment.setName("Amendment B");
        amendment.setDate(createDate(2007, Calendar.OCTOBER, 19));

        study = createNamedInstance(SOURCE_NAME, Study.class);
        study.setPlannedCalendar(calendar);
        study.pushAmendment(amendment);
    }

    @Override
    protected AmendedResource createResource() {
        AmendedResource resource = new AmendedResource();
        resource.setStudyDao(studyDao);
        resource.setAmendmentDao(amendmentDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGet() throws Exception {
        expectFoundStudy();
        expectFoundAmendment();
        expectObjectXmlized(amendment);
        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithCurrentAmendmentIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), CURRENT_AMENDMENT_KEY);

        expectFoundStudy();
        expectObjectXmlized(study.getAmendment());

        doGet();

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

    ////// Expect Methods


    private void expectFoundStudy() {
        expect(studyDao.getByAssignedIdentifier(SOURCE_NAME)).andReturn(study);
    }

    private void expectFoundAmendment() {
        expect(amendmentDao.getByNaturalKey(AMENDMENT_KEY)).andReturn(amendment);
    }

    private void expectStudyNotFound() {
        expect(studyDao.getByAssignedIdentifier("")).andReturn(null);
    }

    private void expectAmendmentNotFound() {
        expect(amendmentDao.getByNaturalKey("")).andReturn(null);
    }


}

