package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResourceTest extends ResourceTestCase<ActivitySourcesResource> {
    private SourceDao sourceDao;
    private ActivityService activityService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityService = registerMockFor(ActivityService.class);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ActivitySourcesResource createResource() {
        ActivitySourcesResource resource = new ActivitySourcesResource();
        resource.setSourceDao(sourceDao);
        resource.setXmlSerializer(xmlSerializer);
        resource.setActivityService(activityService);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    @SuppressWarnings({ "unchecked" })
    public void testGetXmlForAllActivities() throws Exception {
        List<Source> sources = new ArrayList<Source>();
        Source source = new Source();
        sources.add(source);
        expect(sourceDao.getAll()).andReturn(sources);

        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    @SuppressWarnings({ "unchecked" })
    public void testGetXmlForSourcesFilteredByName() throws Exception {
        String search = "day a";
        QueryParameters.Q.putIn(request, search);
        List<Source> sources = Collections.singletonList(Fixtures.createSource("Etc"));
        expect(activityService.getFilteredSources(search, null, null)).andReturn(sources);
        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    @SuppressWarnings({ "unchecked" })
    public void testGetXmlForSourcesFilteredByType() throws Exception {
        QueryParameters.TYPE_ID.putIn(request, "4");
        List<Source> sources = Collections.singletonList(Fixtures.createSource("Etc"));
        expect(activityService.getFilteredSources(null, ActivityType.PROCEDURE, null)).
            andReturn(sources);
        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }
}
