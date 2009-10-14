package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaProviderActivator;
import org.osgi.framework.BundleActivator;

/**
 * @author Rhett Sutphin
 */
public class Activator extends CoppaProviderActivator<DirectCoppaAccessor> implements BundleActivator {
    @Override
    protected DirectCoppaAccessor createCoppaAccessor() {
        return new DirectCoppaAccessor();
    }
}
