package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DefaultCrumb implements Crumb {
    private CrumbSource parent;
    private String name;

    public DefaultCrumb() { }

    public DefaultCrumb(String name) {
        this.name = name;
    }

    public CrumbSource getParent() {
        return parent;
    }

    public void setParent(CrumbSource parent) {
        this.parent = parent;
    }

    public String getName(BreadcrumbContext context) {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParameters(BreadcrumbContext context) {
        return null;
    }
}
