/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.PluginActivator;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;

/**
 * @author Rhett Sutphin
 */
public class Activator extends PluginActivator {
    @Override
    protected Class<? extends AuthenticationSystem> authenticationSystemClass() {
        return WebSSOAuthenticationSystem.class;
    }
}