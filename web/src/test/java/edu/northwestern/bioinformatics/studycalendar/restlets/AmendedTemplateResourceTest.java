package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySnapshotXmlSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Calendar;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.*;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateResourceTest extends AuthorizedResourceTestCase<AmendedTemplateResource> {
    private Study amendedStudy;

    private AmendedTemplateHelper helper;
    private StudySnapshotXmlSerializer studySnapshotXmlSerializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        helper = registerMockFor(AmendedTemplateHelper.class);
        helper.setRequest(request);
        expectLastCall().atLeastOnce();
        studySnapshotXmlSerializer = registerMockFor(StudySnapshotXmlSerializer.class);

        Study study = createBasicTemplate("J");
        study.getAmendment().setReleasedDate(null);
        study.addSite(createSite("A"));
        amendedStudy = study.transientClone();

        expect(helper.isDevelopmentRequest()).andStubReturn(false);
        expect(helper.getAmendedTemplate()).andStubReturn(amendedStudy);
        expect(helper.getReadAuthorizations()).andStubReturn(ResourceAuthorization.createCollection(PscRole.DATA_READER));
    }

    @Override
    protected AmendedTemplateResource createAuthorizedResource() {
        AmendedTemplateResource resource = new AmendedTemplateResource();
        resource.setXmlSerializer(studySnapshotXmlSerializer);
        resource.setAmendedTemplateHelper(helper);
        return resource;
    }

    public void testGetWhenHelperIsSuccessful() throws Exception {
        expect(helper.getAmendedTemplate()).andReturn(amendedStudy);
        expectObjectXmlized();

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSuccessfulResponseIncludesLastModified() throws Exception {
        Date expectedLastMod = DateUtils.createDate(2007, Calendar.APRIL, 6);
        amendedStudy.getAmendment().setReleasedDate(expectedLastMod);
        assertEquals("Sanity check failed", expectedLastMod, amendedStudy.getLastModifiedDate());

        expect(helper.getAmendedTemplate()).andReturn(amendedStudy);
        expectObjectXmlized();

        doGet();

        assertEquals(expectedLastMod, response.getEntity().getModificationDate());
    }

    public void testGetWhenHelperFails() throws Exception {
        expect(helper.getAmendedTemplate()).andThrow(new AmendedTemplateHelper.NotFound("It's not there.  I checked twice."));

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
        assertClientErrorReason("It's not there.  I checked twice.");
    }

    public void testAuthorizedForReadsPerHelper() throws Exception {
        expect(helper.getReadAuthorizations()).andReturn(
            ResourceAuthorization.createCollection(PscRole.DATA_READER, PscRole.DATA_IMPORTER));
        replayMocks();
        assertRolesAllowedForMethod(Method.GET, PscRole.DATA_READER, PscRole.DATA_IMPORTER);
    }

    ////// EXPECTATIONS

    protected void expectObjectXmlized() {
        expect(studySnapshotXmlSerializer.createDocumentString(amendedStudy)).andReturn(MOCK_XML);
    }
}
