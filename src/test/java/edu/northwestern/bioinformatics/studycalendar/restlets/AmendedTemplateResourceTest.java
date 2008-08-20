package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
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
    private static final String STUDY_NAME = "Mutant Study";
    private static final String STUDY_NAME_ENCODED = "Mutant%20Study";

    private static final String AMENDMENT_KEY_ENCODED = "2007-10-19~Amendment%20B";

    private Study study, amendedStudy;
    private Amendment amendment0, amendment1, devAmendment;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private StudySnapshotXmlSerializer studySnapshotXmlSerializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        deltaService = registerMockFor(DeltaService.class);
        studySnapshotXmlSerializer = registerMockFor(StudySnapshotXmlSerializer.class);

        request.getAttributes().put(
            UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_NAME_ENCODED);

        study = createBasicTemplate();
        study.setName(STUDY_NAME);
        amendment0 = study.getAmendment();

        amendment1 = new Amendment();
        amendment1.setName("Amendment B");
        amendment1.setDate(createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(amendment1);

        devAmendment = createAmendment("Amendment C", createDate(2007, Calendar.DECEMBER, 12));
        study.setDevelopmentAmendment(devAmendment);

        amendedStudy = study.transientClone();
    }

    @Override
    protected AmendedTemplateResource createResource() {
        AmendedTemplateResource resource = new AmendedTemplateResource();
        resource.setStudyDao(studyDao);
        resource.setAmendmentDao(amendmentDao);
        resource.setXmlSerializer(studySnapshotXmlSerializer);
        resource.setAmendmentService(amendmentService);
        resource.setDeltaService(deltaService);
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
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AMENDMENT_KEY_ENCODED);

        expectFoundStudy();
        expectFoundAmendment(amendment1);
        expectAmendClonedStudy(amendment1);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithCurrentAmendmentIdentifier() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateResource.CURRENT);

        expectFoundStudy();
        expectAmendClonedStudy(amendment1);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetWithDevelopmentAmendment() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateResource.DEVELOPMENT);

        expectFoundStudy();
        expectRevised(devAmendment);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetForDevelopmentIs404WhenThereIsNoDevelopmentAmendment() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateResource.DEVELOPMENT);
        study.setDevelopmentAmendment(null);

        expectFoundStudy();
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("Mutant Study is not in development");
    }

    public void testGetWithNoStudyIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), "");

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("No study specified");
    }

    public void testGetWithNoAmendmentIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), "");
        expectFoundStudy();

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("No amendment specified");
    }

    public void testGetWithUnassociatedAmendmentIs404() throws Exception {
        Amendment other = new Amendment();
        other.setDate(DateUtils.createDate(2003, Calendar.MARCH, 1));
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), other.getNaturalKey());

        expectFoundStudy();
        expectFoundAmendment(other);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("The amendment 2003-03-01 is not part of Mutant Study");
    }

    ////// EXPECTATIONS

    protected void expectObjectXmlized() {
        expect(studySnapshotXmlSerializer.createDocumentString(amendedStudy)).andReturn(MOCK_XML);
    }

    private void expectFoundStudy() {
        expect(studyDao.getByAssignedIdentifier(STUDY_NAME)).andReturn(study);
    }

    private void expectFoundAmendment(Amendment expected) {
        expect(amendmentDao.getByNaturalKey(expected.getNaturalKey(), study)).andReturn(expected);
    }

    private void expectAmendClonedStudy(Amendment target) {
        expect(amendmentService.getAmendedStudy(eq(study), eq(target))).andReturn(amendedStudy);
    }

    private void expectRevised(Amendment target) {
        expect(deltaService.revise(eq(study), eq(target))).andReturn(amendedStudy);
    }
}
