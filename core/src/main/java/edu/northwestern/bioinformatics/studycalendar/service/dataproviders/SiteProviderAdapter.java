package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;

/**
 * Consolidates calls into all the {@link SiteProvider}s currently live in the system.
 * (but not yet, actually)
 *
 * @author Rhett Sutphin
 */
public class SiteProviderAdapter {
    private static final Class<?> SERVICE = SiteProvider.class;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private SiteProvider emptySiteProvider = new Empty();

    private OsgiLayerTools osgiLayerTools;

    public Site getSite(String assignedIdentifier) {
        return getSiteProvider().getSite(assignedIdentifier);
    }

    public List<Site> search(String partialName) {
        return getSiteProvider().search(partialName);
    }

    private SiteProvider getSiteProvider() {
        SiteProvider sp = (SiteProvider) osgiLayerTools.getOptionalService(SERVICE);
        if (sp == null) {
            log.debug("No {} available in OSGi layer", SERVICE.getName());
            return emptySiteProvider;
        } else {
            return sp;
        }
    }

    /////// CONFIGURATION
    @Required
    public void setOsgiLayerTools(OsgiLayerTools tools) {
        this.osgiLayerTools = tools;
    }

    private static class Empty implements SiteProvider {
        public Site getSite(String assignedIdentifier) { return null; }
        public List<Site> search(String partialName) { return Collections.emptyList(); }
        public String providerToken() { throw new StudyCalendarError("This should never be called"); }
    }
}
