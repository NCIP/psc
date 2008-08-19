package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySnapshotXmlSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.*;
import org.restlet.data.Status;

import java.util.Calendar;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateResourceTest extends AuthorizedResourceTestCase<AmendedTemplateResource> {
    public static final String SOURCE_NAME = "Mutant Study";
    public static final String SOURCE_NAME_ENCODED = "Mutant%20Study";

    private static final String AMENDMENT_KEY = "2007-10-19~Amendment B";
    private static final String AMENDMENT_KEY_ENCODED = "2007-10-19~Amendment%20B";

    private Study study, amendedStudy;
    private Amendment amendment0, amendment1;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private AmendmentService amendmentService;
    private StudySnapshotXmlSerializer studySnapshotXmlSerializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        studySnapshotXmlSerializer = registerMockFor(StudySnapshotXmlSerializer.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), SOURCE_NAME_ENCODED);

        study = Fixtures.createBasicTemplate();
        amendment0 = study.getAmendment();

        amendment1 = new Amendment();
        amendment1.setName("Amendment B");
        amendment1.setDate(createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(amendment1);

        amendedStudy = study.transientClone();
    }

    @Override
    protected AmendedTemplateResource createResource() {
        AmendedTemplateResource resource = new AmendedTemplateResource();
        resource.setStudyDao(studyDao);
        resource.setAmendmentDao(amendmentDao);
        resource.setXmlSerializer(studySnapshotXmlSerializer);
        resource.setAmendmentService(amendmentService);
        return resource;
    }

    public void testGetWithEarlierAmendment() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), amendment0.getNaturalKey());

        expectFoundStudy();
        expectFoundAmendment(amendment0);
        expectAmendClonedStudy(amendment0);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetCurrentAmendmentExplicitly() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AMENDMENT_KEY_ENCODED);

        expectFoundStudy();
        expectFoundAmendment(amendment1);
        expectAmendClonedStudy(amendment1);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithCurrentAmendmentIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AmendedTemplateResource.CURRENT);

        expectFoundStudy();
        expectAmendClonedStudy(amendment1);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithNoStudyIdentifier() {
        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), "");
        expectStudyNotFound();

        doGet();
        assertEquals("Result should be 404", Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testGetWithNoAmendmentIdentifier() {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), "");
        expectAmendmentNotFound();

        expectFoundStudy();

        doGet();
        assertEquals("Result should be 404", Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testGetWithUnassociatedAmendmentIs404() throws Exception {
        Amendment other = new Amendment();
        other.setDate(DateUtils.createDate(2003, Calendar.MARCH, 1));
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), other.getNaturalKey());

        expectFoundStudy();
        expectFoundAmendment(other);

        doGet();
        assertEquals("Result should be 404", Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    ////// EXPECTATIONS

    protected void expectObjectXmlized() {
        expect(studySnapshotXmlSerializer.createDocumentString(amendedStudy)).andReturn(MOCK_XML);
    }

    private void expectFoundStudy() {
        expect(studyDao.getByAssignedIdentifier(SOURCE_NAME)).andReturn(study);
    }

    private void expectFoundAmendment(Amendment expected) {
        expect(amendmentDao.getByNaturalKey(expected.getNaturalKey(), study)).andReturn(expected);
    }

    private void expectStudyNotFound() {
        expect(studyDao.getByAssignedIdentifier("")).andReturn(null);
    }

    private void expectAmendmentNotFound() {
        expect(amendmentDao.getByNaturalKey("", study)).andReturn(null);
    }

    private void expectAmendClonedStudy(Amendment target) {
        expect(amendmentService.getAmendedStudy(eq(study), eq(target))).andReturn(amendedStudy);
    }
}
