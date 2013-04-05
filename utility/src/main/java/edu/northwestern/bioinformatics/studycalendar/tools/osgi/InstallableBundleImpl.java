/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

/**
* @author Rhett Sutphin
*/
public class InstallableBundleImpl implements InstallableBundle {
    private int startLevel;
    private String location;
    private boolean shouldStart;

    public InstallableBundleImpl(int startLevel, String location, boolean shouldStart) {
        this.startLevel = startLevel;
        this.location = location;
        this.shouldStart = shouldStart;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public String getLocation() {
        return location;
    }

    public boolean getShouldStart() {
        return shouldStart;
    }
}
