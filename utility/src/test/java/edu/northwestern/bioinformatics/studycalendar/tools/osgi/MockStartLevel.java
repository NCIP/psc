/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import org.osgi.framework.Bundle;
import org.osgi.service.startlevel.StartLevel;

import java.util.IdentityHashMap;
import java.util.Map;

/**
* @author Rhett Sutphin
*/
class MockStartLevel implements StartLevel {
    private Map<Bundle, Integer> bundleStartLevels = new IdentityHashMap<Bundle, Integer>();
    private int initialBundleStartLevel = 1;
    private int startLevel = 1;

    public int getBundleStartLevel(Bundle bundle) {
        Integer actual = bundleStartLevels.get(bundle);
        return actual == null ? getInitialBundleStartLevel() : actual;
    }

    public void setBundleStartLevel(Bundle bundle, int i) {
        bundleStartLevels.put(bundle, i);
    }

    public int getInitialBundleStartLevel() {
        return initialBundleStartLevel;
    }

    public void setInitialBundleStartLevel(int i) {
        this.initialBundleStartLevel = i;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public void setStartLevel(int i) {
        startLevel = i;
    }

    public boolean isBundlePersistentlyStarted(Bundle bundle) {
        throw new UnsupportedOperationException("isBundlePersistentlyStarted not implemented");
    }

    public boolean isBundleActivationPolicyUsed(Bundle bundle) {
        throw new UnsupportedOperationException("isBundleActivationPolicyUsed not implemented");
    }
}
