/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.insecure;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.PluginActivator;

/**
 * @author Rhett Sutphin
 */
public class Activator extends PluginActivator {
    @Override
    protected Class<? extends AuthenticationSystem> authenticationSystemClass() {
        return InsecureAuthenticationSystem.class;
    }
}
