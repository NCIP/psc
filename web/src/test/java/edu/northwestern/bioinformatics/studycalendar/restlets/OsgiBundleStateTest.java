/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import junit.framework.TestCase;
import org.osgi.framework.Bundle;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleStateTest extends TestCase {
    public void testOsgiConstantMappedCorrectly() throws Exception {
        assertEquals(OsgiBundleState.ACTIVE.constant(),      Bundle.ACTIVE);
        assertEquals(OsgiBundleState.UNINSTALLED.constant(), Bundle.UNINSTALLED);
        assertEquals(OsgiBundleState.INSTALLED.constant(),   Bundle.INSTALLED);
        assertEquals(OsgiBundleState.STARTING.constant(),    Bundle.STARTING);
        assertEquals(OsgiBundleState.STOPPING.constant(),    Bundle.STOPPING);
        assertEquals(OsgiBundleState.RESOLVED.constant(),    Bundle.RESOLVED);
    }

    public void testGetFromBundleConstant() throws Exception {
        assertSame(OsgiBundleState.STOPPING, OsgiBundleState.valueOfConstant(Bundle.STOPPING));
        assertSame(OsgiBundleState.ACTIVE, OsgiBundleState.valueOfConstant(Bundle.ACTIVE));
    }

    public void testGetFromBundleConstantForUnknownThrowsException() throws Exception {
        try {
            OsgiBundleState.valueOfConstant(11);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Unknown bundle state 0xB", iae.getMessage());
        }
    }
}
