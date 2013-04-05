/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.springframework.osgi.mock.MockBundle;

import java.util.Map;

/**
* @author Rhett Sutphin
*/
class RecordingMockBundle extends MockBundle {
    private Integer startFlags;

    RecordingMockBundle(String symName, BundleContext context) {
        super(symName, null, context);
    }

    @Override
    public void start(int options) throws BundleException {
        startFlags = options;
    }

    public boolean isStarted() {
        return startFlags != null;
    }

    public Integer getStartFlags() {
        return startFlags;
    }

    public Map getSignerCertificates(int i) {
        throw new UnsupportedOperationException("getSignerCertificates not implemented");
    }

    public Version getVersion() {
        throw new UnsupportedOperationException("getVersion not implemented");
    }
}
