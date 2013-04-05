/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi;

import edu.northwestern.bioinformatics.studycalendar.restlets.OsgiBundleState;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import junit.framework.TestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public abstract class OsgiLayerIntegratedTestCase extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DataAuditInfo.setLocal(new DataAuditInfo("jo", "127.0.0.8", new Date(),
            "/the/url"));
    }

    @Override
    protected void tearDown() throws Exception {
        DataAuditInfo.setLocal(null);
        super.tearDown();
    }

    protected void dumpBundles() throws IOException {
        for (Bundle bundle : OsgiLayerIntegratedTestHelper.getBundleContext().getBundles()) {
            System.out.println(
                String.format("%3d %10s %s",
                    bundle.getBundleId(),
                    OsgiBundleState.valueOfConstant(bundle.getState()),
                    bundle.getSymbolicName()));
            if (bundle.getRegisteredServices() != null) {
                for (ServiceReference reference : bundle.getRegisteredServices()) {
                    Object sid = reference.getProperty("service.id");
                    String[] interfaces = (String[]) reference.getProperty("objectClass");
                    System.out.println(String.format("%14s %s", sid, Arrays.asList(interfaces)));
                    if (reference.getPropertyKeys() != null) {
                        for (String key : reference.getPropertyKeys()) {
                            System.out.println(
                                String.format("               - %s = %s", key, reference.getProperty(key)));
                        }
                    }
                }
            }
        }
    }
}
