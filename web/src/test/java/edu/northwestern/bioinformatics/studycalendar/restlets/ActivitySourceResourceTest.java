package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.restlet.data.Method;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

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
}
