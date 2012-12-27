/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

/**
 * @author Rhett Sutphin
*/
class TestCrumbSource implements CrumbSource {
    private Crumb crumb;

    public TestCrumbSource(Crumb crumb) {
        this.crumb = crumb;
    }

    public Crumb getCrumb() {
        return crumb;
    }
}
