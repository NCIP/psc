/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
* @author Rhett Sutphin
*/
class InstallingMockBundleContext extends MockBundleContext {
    private List<RecordingMockBundle> bundles = new ArrayList<RecordingMockBundle>();
    private StartLevel startLevelService;

    InstallingMockBundleContext(StartLevel startLevelService) {
        this.startLevelService = startLevelService;
    }

    public List<RecordingMockBundle> bundlesInstalled() {
        return bundles;
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        RecordingMockBundle installed = new RecordingMockBundle("The bundle from " + location, this);
        installed.setLocation(location);
        bundles.add(installed);
        return installed;
    }

    @Override
    public ServiceReference getServiceReference(String clazz) {
        if (clazz.equals(StartLevel.class.getName())) {
            return new MockServiceReference();
        } else {
            return null;
        }
    }

    @Override
    public Object getService(ServiceReference reference) {
        if (reference == null) throw new NullPointerException("Try again");
        return startLevelService;
    }
}
