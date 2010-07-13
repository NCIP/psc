package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResourceTest extends AuthorizedResourceTestCase<ActivitySourcesResource> {
    private SourceDao sourceDao;
    private ActivityService activityService;
    private ActivityTypeDao activityTypeDao;
    private ActivityType procedureType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        activityService = registerMockFor(ActivityService.class);
        procedureType = setId(4, createActivityType("Procedure"));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ActivitySourcesResource createAuthorizedResource() {
        ActivitySourcesResource resource = new ActivitySourcesResource();
        resource.setSourceDao(sourceDao);
        resource.setXmlSerializer(xmlSerializer);
        resource.setActivityService(activityService);
        resource.setActivityTypeDao(activityTypeDao);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_CALENDAR_TEMPLATE_BUILDER,
            BUSINESS_ADMINISTRATOR,
            DATA_READER);
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
        List<Source> sources = Collections.singletonList(createSource("Etc"));
        expect(activityService.getFilteredSources(search, null, null)).andReturn(sources);
        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    @SuppressWarnings({ "unchecked" })
    @Deprecated /* To remove in 2.6 or so */
    public void testGetXmlForSourcesFilteredByTypeId() throws Exception {
        QueryParameters.TYPE_ID.putIn(request, "4");
        List<Source> sources = Collections.singletonList(createSource("Etc"));
        expect(activityService.getFilteredSources(null, procedureType, null)).andReturn(sources);
        expect(activityTypeDao.getById(4)).andReturn(procedureType);
        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    @SuppressWarnings({ "unchecked" })
    public void testGetXmlForSourcesFilteredByType() throws Exception {
        QueryParameters.TYPE.putIn(request, "Procedure");
        List<Source> sources = Collections.singletonList(createSource("Etc"));
        expect(activityTypeDao.getByName("Procedure")).andReturn(procedureType);
        expect(activityService.getFilteredSources(null, procedureType, null)).andReturn(sources);
        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetXmlWithUnknownActivityType() throws Exception {
        QueryParameters.TYPE.putIn(request, "Proc");
        expect(activityTypeDao.getByName("Proc")).andReturn(null);

        doGet();
        assertResponseStatus(new Status(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown activity type: Proc"));
    }
}
