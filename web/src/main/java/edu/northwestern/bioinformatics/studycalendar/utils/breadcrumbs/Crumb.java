/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;

/**
 * @author Rhett Sutphin
 */
public interface Crumb {
    CrumbSource getParent();
    String getName(DomainContext context);
    Map<String, String> getParameters(DomainContext context);
}
