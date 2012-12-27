/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;

/**
 * @author Rhett Sutphin
 */
public abstract class CoppaProviderActivator<T extends CoppaAccessor & ManagedService> implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        createCoppaAccessor().register(bundleContext);
        bundleContext.registerService(SiteProvider.class.getName(),
            new CoppaSiteProvider(bundleContext), null);
        bundleContext.registerService(StudyProvider.class.getName(),
            new CoppaStudyProvider(bundleContext), null);
        bundleContext.registerService(StudySiteProvider.class.getName(),
            new CoppaStudySiteProvider(bundleContext), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }

    protected abstract T createCoppaAccessor();
}
