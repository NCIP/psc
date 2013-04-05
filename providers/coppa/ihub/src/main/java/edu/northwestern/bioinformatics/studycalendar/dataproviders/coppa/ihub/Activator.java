/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaProviderActivator;
import org.osgi.framework.BundleActivator;

/**
 * @author Rhett Sutphin
 */
public class Activator extends CoppaProviderActivator<IntegrationHubCoppaAccessor> implements BundleActivator {
    @Override
    protected IntegrationHubCoppaAccessor createCoppaAccessor() {
        return new IntegrationHubCoppaAccessor();
    }
}