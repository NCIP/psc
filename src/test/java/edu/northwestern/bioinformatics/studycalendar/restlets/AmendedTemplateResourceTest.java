package edu.northwestern.bioinformatics.studycalendar.restlets;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createBasicTemplate;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySnapshotXmlSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.Calendar;
import java.util.Date;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateResourceTest extends AuthorizedResourceTestCase<AmendedTemplateResource> {
    private Study study, amendedStudy;

    private AmendedTemplateHelper helper;
    private StudySnapshotXmlSerializer studySnapshotXmlSerializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        helper = registerMockFor(AmendedTemplateHelper.class);
        studySnapshotXmlSerializer = registerMockFor(StudySnapshotXmlSerializer.class);

        study = createBasicTemplate();
        amendedStudy = study.transientClone();
    }

    @Override
    protected AmendedTemplateResource createResource() {
        AmendedTemplateResource resource = new AmendedTemplateResource();
        resource.setXmlSerializer(studySnapshotXmlSerializer);
        resource.setAmendedTemplateHelper(helper);
        return resource;
    }

    public void testGetWhenHelperIsSuccessful() throws Exception {
        expect(helper.getAmendedTemplate(request)).andReturn(amendedStudy);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSuccessfulResponseIncludesLastModified() throws Exception {
        Date expectedLastMod = DateUtils.createDate(2007, Calendar.APRIL, 6);
        amendedStudy.getAmendment().setUpdatedDate(expectedLastMod);
        assertEquals("Sanity check failed", expectedLastMod, amendedStudy.getLastModifiedDate());

        expect(helper.getAmendedTemplate(request)).andReturn(amendedStudy);
        expectObjectXmlized();

        doGet();

        assertEquals(expectedLastMod, response.getEntity().getModificationDate());
    }

    public void testGetWhenHelperFails() throws Exception {
        expect(helper.getAmendedTemplate(request)).andThrow(new AmendedTemplateHelper.NotFound("It's not there.  I checked twice."));

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertEntityTextContains("404");
        assertEntityTextContains("It's not there.  I checked twice.");
    }

    ////// EXPECTATIONS

    protected void expectObjectXmlized() {
        expect(studySnapshotXmlSerializer.createDocumentString(amendedStudy)).andReturn(MOCK_XML);
    }
}
