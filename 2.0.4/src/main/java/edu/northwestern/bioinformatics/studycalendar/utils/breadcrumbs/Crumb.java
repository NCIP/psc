package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import org.springframework.web.servlet.mvc.Controller;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public interface Crumb {
    CrumbSource getParent();
    String getName(BreadcrumbContext context);
    Map<String, String> getParameters(BreadcrumbContext context);
}
