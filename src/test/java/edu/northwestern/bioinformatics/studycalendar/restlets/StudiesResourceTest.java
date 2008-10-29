package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudiesResourceTest extends ResourceTestCase<StudiesResource> {
    private StudyDao studyDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected StudiesResource createResource() {
        StudiesResource res = new StudiesResource();
        res.setStudyDao(studyDao);
        res.setXmlSerializer(xmlSerializer);
        return res;
    }

    public void testIsReadOnly() throws Exception {
        assertAllowedMethods("GET", "POST");
    }

    public void testAllRenderedOut() throws Exception {
        List<Study> expectedStudies = Arrays.asList(Fixtures.createBasicTemplate());
        expect(studyDao.getAll()).andReturn(expectedStudies);
        expect(xmlSerializer.createDocumentString(expectedStudies)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }
}
