package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Jalpa Patel
 */
public class ProvidedStudiesResourceTest extends ResourceTestCase<ProvidedStudiesResource> {
    private StudyConsumer studyConsumer;

    public void setUp() throws Exception {
        super.setUp();
        studyConsumer = registerMockFor(StudyConsumer.class);
    }

    protected ProvidedStudiesResource createResource() {
        ProvidedStudiesResource resource = new ProvidedStudiesResource();
        resource.setXmlSerializer(xmlSerializer);
        resource.setStudyConsumer(studyConsumer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetAllProvidedStudies() throws Exception {
        String searchString = "s";
        QueryParameters.Q.putIn(request, searchString);
        List<Study> studies = new ArrayList<Study>();
        Study study = Fixtures.createBasicTemplate("Study");
        studies.add(study);
        expect(studyConsumer.search(searchString)).andReturn(Collections.unmodifiableList(studies));
        expect(xmlSerializer.createDocumentString(studies)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

}
