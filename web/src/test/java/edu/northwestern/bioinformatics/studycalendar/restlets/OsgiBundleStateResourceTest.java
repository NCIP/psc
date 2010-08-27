package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;
import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleStateResourceTest extends AuthorizedResourceTestCase<OsgiBundleStateResource> {
    private static long BUNDLE_ID = 74;

    private Bundle bundle;
    private BundleContext bundleContext;

    @Override
    protected OsgiBundleStateResource createAuthorizedResource() {
        OsgiBundleStateResource resource = new OsgiBundleStateResource();
        resource.setBundleContext(bundleContext);
        return resource;
    }

    @Override
    protected void setUp() throws Exception {
        bundle = registerMockFor(Bundle.class);
        bundleContext = registerMockFor(BundleContext.class);
        super.setUp();
        request.getAttributes().put("bundle-id", Long.toString(BUNDLE_ID));
    }

    public void testAllowedMethods() throws Exception {
        assertAllowedMethods("GET", "PUT");
    }
    
    public void testSysAdminsOnlyCanGet() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        assertRolesAllowedForMethod(Method.GET, SYSTEM_ADMINISTRATOR);
    }

    public void testSysAdminsOnlyCanPut() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        assertRolesAllowedForMethod(Method.PUT, SYSTEM_ADMINISTRATOR);
    }

    public void testGetCurrentState() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("{\"state\":\"ACTIVE\"}", response.getEntity().getText());
    }

    public void testGet404ForUnknownBundleId() throws Exception {
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(null);
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPut404ForUnknownBundleId() throws Exception {
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(null);
        putToState("STARTING");
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void test400ForNonNumericBundleId() throws Exception {
        request.getAttributes().put("bundle-id", "seventy-four");
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void test400ForInvalidState() throws Exception {
        expectGetBundleInState(Bundle.RESOLVED);
        putToState("STARING");
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void test400ForUnsupportedEntityContentType() throws Exception {
        request.setEntity("state=STARTING", MediaType.TEXT_PLAIN);
        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    ////// PUT to STARTING

    public void testSetBundleToStartingFromInstalled() throws Exception {
        expectGetBundleInState(Bundle.INSTALLED);
        bundle.start();
        putToState("STARTING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStartingFromResolved() throws Exception {
        expectGetBundleInState(Bundle.RESOLVED);
        bundle.start();
        putToState("STARTING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStartingFromStopping() throws Exception {
        expectGetBundleInState(Bundle.STOPPING);
        bundle.start();
        putToState("STARTING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStartingFromStarting() throws Exception {
        expectGetBundleInState(Bundle.STARTING);
        // nothing should happen
        putToState("STARTING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStartingFromActive() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        // nothing should happen
        putToState("STARTING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testStartFails() throws Exception {
        expectGetBundleInState(Bundle.INSTALLED);
        bundle.start();
        expectLastCall().andThrow(new BundleException("Couldn't start for some reason"));
        putToState("STARTING");
        assertResponseStatus(Status.SERVER_ERROR_INTERNAL);
    }

    ////// PUT to STOPPING

    public void testSetBundleToStoppingFromInstalled() throws Exception {
        expectGetBundleInState(Bundle.INSTALLED);
        // nothing should happen
        putToState("STOPPING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStoppingFromResolved() throws Exception {
        expectGetBundleInState(Bundle.RESOLVED);
        // nothing should happen
        putToState("STOPPING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStoppingFromStopping() throws Exception {
        expectGetBundleInState(Bundle.STOPPING);
        // nothing should happen
        putToState("STOPPING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStoppingFromStarting() throws Exception {
        expectGetBundleInState(Bundle.STARTING);
        bundle.stop();
        putToState("STOPPING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToStoppingFromActive() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        bundle.stop();
        putToState("STOPPING");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testStopFails() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        bundle.stop();
        expectLastCall().andThrow(new BundleException("Couldn't stop for some reason"));
        putToState("STOPPING");
        assertResponseStatus(Status.SERVER_ERROR_INTERNAL);
    }

    ////// PUT is idempotent for other states

    public void testSetBundleToInstalledFromInstalledIsNoOp() throws Exception {
        expectGetBundleInState(Bundle.INSTALLED);
        putToState("INSTALLED");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToUninstalledFromUninstalledIsNoOp() throws Exception {
        expectGetBundleInState(Bundle.UNINSTALLED);
        putToState("UNINSTALLED");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToResolvedFromResolvedIsNoOp() throws Exception {
        expectGetBundleInState(Bundle.RESOLVED);
        putToState("RESOLVED");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testSetBundleToActiveFromActiveIsNoOp() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        putToState("ACTIVE");
        assertResponseStatus(Status.SUCCESS_OK);
    }

    ////// PUT into any other state is not allowed

    public void testSetBundleToResolvedFromOtherStatesIs422() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        putToState("RESOLVED");
        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
    }

    public void testSetBundleToInstalledFromOtherStatesIs422() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        putToState("INSTALLED");
        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
    }

    public void testSetBundleToUninstalledFromOtherStatesIs422() throws Exception {
        expectGetBundleInState(Bundle.ACTIVE);
        putToState("UNINSTALLED");
        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
    }

    public void testSetBundleToActiveFromOtherStatesIs422() throws Exception {
        expectGetBundleInState(Bundle.INSTALLED);
        putToState("ACTIVE");
        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
    }

    ////// HELPERS

    private void expectGetBundleInState(int state) {
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle).anyTimes();
        expect(bundle.getState()).andStubReturn(state);
    }

    private void putToState(String state) {
        request.setEntity(String.format("{ state: %s }", state), MediaType.APPLICATION_JSON);
        doPut();
    }
}
