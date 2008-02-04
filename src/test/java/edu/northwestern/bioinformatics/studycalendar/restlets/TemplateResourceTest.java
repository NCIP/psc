package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;
import static org.easymock.classextension.EasyMock.*;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public class TemplateResourceTest extends ResourceTestCase<TemplateResource> {
    private static final String STUDY_IDENT = "elf";

    private StudyDao studyDao;
    private Study study;

    public void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);

        request.getAttributes().put(STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
        study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(STUDY_IDENT, "Treatment")));
        Fixtures.assignIds(study);
    }

    protected TemplateResource createResource() {
        TemplateResource res = new TemplateResource();
        res.setStudyDao(studyDao);
        res.setXmlSerializer(xmlSerializer);
        return res;
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

    /*
    // These tests should be corrected and uncommented when PUT is implemented.
    public void testPutExistingXml() throws Exception {
        Study newStudy = new Study();
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        expectReadXmlFromRequestAs(newStudy);
        expectObjectXmlized(newStudy);

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
        expectReadXmlFromRequestAs(study);
        expectObjectXmlized(study);

        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }
    */
}
