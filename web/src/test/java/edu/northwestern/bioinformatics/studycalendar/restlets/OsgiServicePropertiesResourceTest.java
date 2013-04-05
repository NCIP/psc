/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.test.osgi.PscMockBundle;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.mock.MockServiceReference;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class OsgiServicePropertiesResourceTest extends AuthorizedResourceTestCase<OsgiServicePropertiesResource> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final Integer BUNDLE_ID = 14;

    private BundleContext bundleContext;
    private OsgiLayerTools osgiLayerTools;
    private PscMockBundle bundle;

    @Override
    protected OsgiServicePropertiesResource createAuthorizedResource() {
        OsgiServicePropertiesResource resource = new OsgiServicePropertiesResource();
        resource.setOsgiLayerTools(osgiLayerTools);
        resource.setBundleContext(bundleContext);
        return resource;
    }

    @Override
    protected void setUp() throws Exception {
        bundleContext = registerMockFor(BundleContext.class);
        osgiLayerTools = registerMockFor(OsgiLayerTools.class);

        super.setUp();

        bundle = new PscMockBundle();
        UriTemplateParameters.BUNDLE_ID.putIn(request, BUNDLE_ID.toString());
        expect(bundleContext.getBundle(BUNDLE_ID)).andStubReturn(bundle);
    }

    public void testPutAndGetAllowed() throws Exception {
        assertAllowedMethods("GET", "PUT");
    }

    public void testSysAdminsOnlyCanGet() throws Exception {
        assertRolesAllowedForMethod(Method.GET, PscRole.SYSTEM_ADMINISTRATOR);
    }

    public void testSysAdminsOnlyCanPut() throws Exception {
        assertRolesAllowedForMethod(Method.PUT, PscRole.SYSTEM_ADMINISTRATOR);
    }

    public void test404ForInvalidBundle() throws Exception {
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND, "No bundle with ID 14");
    }

    public void test400ForBadBundleId() throws Exception {
        UriTemplateParameters.BUNDLE_ID.putIn(request, "foo");
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid bundle ID foo");
    }

    public void test404ForBundleWithoutServices() throws Exception {
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND, "No services for bundle");
    }

    public void testGetForKnownServiceById() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "66");
        expectService(propBuilder().put(Constants.SERVICE_ID, 60L).put("prop", "A"));
        expectService(propBuilder().put(Constants.SERVICE_ID, 66L).put("prop", "B"));

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        JSONObject actual = new JSONObject(response.getEntity().getText());
        log.debug(actual.toString());
        assertEquals("Wrong service properties returned: " + actual, "B", actual.get("prop"));
    }

    public void testGetForKnownServiceByPid() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "alpha");
        expectService(propBuilder().put(Constants.SERVICE_PID, "alpha").put("prop", "A"));
        expectService(propBuilder().put(Constants.SERVICE_PID, "beta").put("prop", "B"));

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        JSONObject actual = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong service properties returned: " + actual, "A", actual.get("prop"));
    }

    public void testGetWithUnknownIdentifier() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "unk");
        expectService(propBuilder().put(Constants.SERVICE_PID, "alpha").put("prop", "A"));
        expectService(propBuilder().put(Constants.SERVICE_PID, "beta").put("prop", "B"));

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPutToManagedService() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "alpha");
        expectService(propBuilder().put(Constants.SERVICE_PID, "alpha"),
            "java.util.List", ManagedService.class.getName());
        setRequestEntity(propBuilder().put("prop", "C"));

        osgiLayerTools.updateConfiguration(propBuilder().put("prop", "C").toDictionary(), bundle, "alpha");

        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void test400ForPutToNonManagedService() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "alpha");
        expectService(propBuilder().put(Constants.SERVICE_PID, "alpha"), "java.util.List");
        setRequestEntity(propBuilder().put("prop", "C"));

        doPut();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "The selected service is not manageable.  It does not export ManagedService");
    }

    public void testPutWhenIdentifiedByServiceId() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "18");
        expectService(
            propBuilder().put(Constants.SERVICE_PID, "alpha").put(Constants.SERVICE_ID, 18L),
            ManagedService.class.getName());
        setRequestEntity(propBuilder().put("foo", "quux"));

        osgiLayerTools.updateConfiguration(
            propBuilder().put("foo", "quux").toDictionary(), bundle, "alpha");
        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testPutWithoutServicePid() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "18");
        expectService(
            propBuilder().put(Constants.SERVICE_ID, 18L),
            ManagedService.class.getName());
        setRequestEntity(propBuilder().put("foo", "quux"));

        doPut();

        assertResponseStatus(Status.SERVER_ERROR_INTERNAL, "No service.pid for managed service");
    }

    public void testPutFiltersOutOsgiProperties() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "alpha");
        expectService(propBuilder().put(Constants.SERVICE_PID, "alpha"),
            ManagedService.class.getName());
        setRequestEntity(propBuilder().
            put("prop", "C").
            put(Constants.SERVICE_ID, 7L).
            put(Constants.SERVICE_PID, "gamma").
            put(Constants.SERVICE_RANKING, 12).
            put("other_prop", "T").
            put(Constants.OBJECTCLASS, new String[] { java.util.Collection.class.getName() })
        );

        osgiLayerTools.updateConfiguration(
            propBuilder().put("prop", "C").put("other_prop", "T").toDictionary(), bundle, "alpha");

        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testPutFlattensNestedObjects() throws Exception {
        UriTemplateParameters.SERVICE_IDENTIFIER.putIn(request, "alpha");
        expectService(propBuilder().put(Constants.SERVICE_PID, "alpha"),
            ManagedService.class.getName());
        setRequestEntity(propBuilder().
            put("a", new JSONObject(
                propBuilder().put("b", "c").put("d", new JSONObject(
                    propBuilder().put("e", 10).toMap())).toMap()))
        );

        osgiLayerTools.updateConfiguration(
            propBuilder().put("a.b", "c").put("a.d.e", 10).toDictionary(), bundle, "alpha");

        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    private void setRequestEntity(MapBuilder<String, Object> builder) {
        request.setEntity(new JSONObject(builder.toMap()).toString(), MediaType.APPLICATION_JSON);
    }

    private ServiceReference expectService(MapBuilder<String, Object> properties, String... interfaces) {
        MockServiceReference ref = new MockServiceReference(
            bundle, properties.toDictionary(), null, interfaces);
        bundle.addRegisteredService(ref);
        return ref;
    }

    private static MapBuilder<String, Object> propBuilder() {
        return new MapBuilder<String, Object>();
    }
}
