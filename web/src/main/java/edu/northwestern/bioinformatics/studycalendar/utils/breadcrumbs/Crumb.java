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
