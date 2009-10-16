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