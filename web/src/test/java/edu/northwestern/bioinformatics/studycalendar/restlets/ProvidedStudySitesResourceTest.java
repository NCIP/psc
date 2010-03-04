package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import static java.util.Arrays.asList;

public class ProvidedStudySitesResourceTest extends ResourceTestCase<ProvidedStudySitesResource> {
    private StudySiteConsumer studySiteConsumer;
    private Study nu123;
    private Study nci999;
    private String NU_IDENT;
    private Site site;

    public void setUp() throws Exception {
        super.setUp();
        studySiteConsumer = registerMockFor(StudySiteConsumer.class);

        nu123 = createStudy("NU-123");
        nci999 = createStudy("NCI-999");

        NU_IDENT = "NU";
        site = Fixtures.createSite(NU_IDENT, NU_IDENT);
    }

    @SuppressWarnings({"unchecked"})
    protected ProvidedStudySitesResource createResource() {
        ProvidedStudySitesResource resource = new ProvidedStudySitesResource();
        resource.setXmlSerializer(xmlSerializer);
        resource.setStudySiteConsumer(studySiteConsumer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetAllProvidedStudiesForSite() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), NU_IDENT);

        expect(studySiteConsumer.refresh(site)).andReturn(asList(
            providedStudySite(nu123),
            providedStudySite(nci999)
        ));

        expect(xmlSerializer.createDocumentString(asList(nu123, nci999))).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    ///////////// Helper Methods
    private StudySite providedStudySite(Study study) {
        return new StudySite(study, null);
    }


    private Study createStudy(String id) {
        Study study = new Study();
        study.setAssignedIdentifier(id);
        return study;
    }
}
