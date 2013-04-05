/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;

/**
 * @author Rhett Sutphin
 */
public interface HostBeans {
    void setPscUserDetailsService(PscUserDetailsService userDetailsService);
}
