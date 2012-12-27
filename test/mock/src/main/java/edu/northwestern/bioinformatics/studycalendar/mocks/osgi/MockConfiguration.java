/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.mocks.osgi;

import org.osgi.service.cm.Configuration;

import java.util.Dictionary;
import java.io.IOException;

/**
 * @author Rhett Sutphin
*/
public class MockConfiguration implements Configuration {
    public String getPid() {
        throw new UnsupportedOperationException("getPid not implemented");
    }

    public Dictionary getProperties() {
        throw new UnsupportedOperationException("getProperties not implemented");
    }

    public void update(Dictionary dictionary) throws IOException {
        // do nothing
    }

    public void delete() throws IOException {
        // do nothing
    }

    public String getFactoryPid() {
        throw new UnsupportedOperationException("getFactoryPid not implemented");
    }

    public void update() throws IOException {
        // do nothing
    }

    public void setBundleLocation(String s) {
        throw new UnsupportedOperationException("setBundleLocation not implemented");
    }

    public String getBundleLocation() {
        throw new UnsupportedOperationException("getBundleLocation not implemented");
    }
}
