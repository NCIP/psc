package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static org.easymock.classextension.EasyMock.expect;

import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceResourceTest extends AuthorizedResourceTestCase<ActivitySourceResource> {
    public static final String SOURCE_NAME = "House of Activities";
    public static final String SOURCE_NAME_ENCODED = "House%20of%20Activities";

    private SourceDao sourceDao;

    private Source source;

    private SourceService sourceService;
    private ActivityService activityService;
    private ActivityTypeDao activityTypeDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        sourceService = registerMockFor(SourceService.class);
        activityService = registerMockFor(ActivityService.class);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_SOURCE_NAME.attributeName(), SOURCE_NAME_ENCODED);

        source = Fixtures.createNamedInstance(SOURCE_NAME, Source.class);
    }

    @Override
    protected ActivitySourceResource createAuthorizedResource() {
        ActivitySourceResource resource = new ActivitySourceResource();
        resource.setSourceDao(sourceDao);
        resource.setXmlSerializer(xmlSerializer);
        resource.setSourceService(sourceService);
        resource.setActivityService(activityService);
        resource.setActivityTypeDao(activityTypeDao);
        return resource;
    }

    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_CALENDAR_TEMPLATE_BUILDER,
            BUSINESS_ADMINISTRATOR,
            DATA_READER);
    }

    public void testPutWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.PUT,
            BUSINESS_ADMINISTRATOR);
    }

    public void testGetXmlForExistingSource() throws Exception {
        expectFoundSource(source);
        expectObjectXmlized(source);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForNonExistentSourceIs404() throws Exception {
        expectFoundSource(null);

        doGet();

        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }

    public void testGetFiltersByQIfProvided() throws Exception {
        QueryParameters.Q.putIn(request, "etc");
        Source expectedSource = source.transientClone();

        expectFoundSource(source);
        expect(activityService.getFilteredSources("etc", null, source)).
            andReturn(Collections.singletonList(expectedSource));
        expectObjectXmlized(expectedSource);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetFiltersByTypeIdIfProvided() throws Exception {
        QueryParameters.TYPE_ID.putIn(request, "3");
        Source expectedSource = source.transientClone();
        expect(activityTypeDao.getById(3)).andReturn(new ActivityType("LAB_TEST")).anyTimes();
        expectFoundSource(source);
        expect(activityService.getFilteredSources(null, Fixtures.createActivityType("LAB_TEST"), source)).
            andReturn(Collections.singletonList(expectedSource));
        expectObjectXmlized(expectedSource);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetFiltersByTypeNameIfProvided() throws Exception {
        QueryParameters.TYPE.putIn(request, "LAB_TEST");
        Source expectedSource = source.transientClone();
        expect(activityTypeDao.getByName("LAB_TEST")).andReturn(new ActivityType("LAB_TEST")).anyTimes();
        expectFoundSource(source);
        expect(activityService.getFilteredSources(null,  Fixtures.createActivityType("LAB_TEST"), source)).
            andReturn(Collections.singletonList(expectedSource));
        expectObjectXmlized(expectedSource);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetRendersEmptySourceIfFilteringComesUpWithNothing() throws Exception {
        QueryParameters.TYPE_ID.putIn(request, "3");
        Source expectedSource = Fixtures.createSource(source.getName());
        expect(activityTypeDao.getById(3)).andReturn(new ActivityType("LAB_TEST")).anyTimes();
        expectFoundSource(source);
        expect(activityService.getFilteredSources(null, Fixtures.createActivityType("LAB_TEST"), source)).
            andReturn(Collections.<Source>emptyList());
        expectObjectXmlized(expectedSource);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testPutExistingSource() throws Exception {
        Source newSource = new Source();
        expectFoundSource(source);
        expectReadXmlFromRequestAs(newSource);
        expectObjectXmlized(newSource);

        sourceService.updateSource(newSource, source);

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expectFoundSource(null);
        expectObjectXmlized(source);
        expectReadXmlFromRequestAs(source);

        sourceDao.save(source);
        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }


    private void expectFoundSource(Source expectedSource) {
        expect(sourceDao.getByName(SOURCE_NAME)).andReturn(expectedSource);
    }
}
