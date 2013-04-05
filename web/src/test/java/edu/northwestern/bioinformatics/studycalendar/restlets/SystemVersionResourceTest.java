/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import gov.nih.nci.cabig.ctms.tools.BuildInfo;
import org.json.JSONObject;
import org.restlet.data.Method;

/**
 * @author Rhett Sutphin
 */
public class SystemVersionResourceTest extends AuthorizedResourceTestCase<SystemVersionResource> {
    private BuildInfo buildInfo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        buildInfo = new BuildInfo();
        buildInfo.setVersionNumber("2.3.5.FOO");
    }

    @Override
    protected SystemVersionResource createAuthorizedResource() {
        SystemVersionResource r = new SystemVersionResource();
        r.setBuildInfo(buildInfo);
        return r;
    }

    public void testAuthorizedForAnyoneAuthenticated() throws Exception {
        assertRolesAllowedForMethod(Method.GET, PscRole.values());
    }

    public void testIsForGetOnly() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testEntityContainsVersionNumber() throws Exception {
        doGet();

        JSONObject actual = new JSONObject(response.getEntity().getText());
        assertEquals("2.3.5.FOO", actual.optString("psc_version"));
    }
}
