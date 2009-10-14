package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(SiteProvider.class.getName(), new CoppaSiteProvider(), null);
        bundleContext.registerService(StudyProvider.class.getName(),
            new CoppaStudyProvider(), null);
        bundleContext.registerService(StudySiteProvider.class.getName(),
            new CoppaStudySiteProvider(), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
