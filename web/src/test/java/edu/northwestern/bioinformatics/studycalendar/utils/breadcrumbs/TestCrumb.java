package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import java.util.Map;

/**
 * @author Rhett Sutphin
*/
class TestCrumb implements Crumb {
    private String name;
    private CrumbSource parent;
    private Map<String, String> params;

    public TestCrumb(String name, Map<String, String> params, Crumb parent) {
        this.name = name;
        this.parent = parent == null ? null : new TestCrumbSource(parent);
        this.params = params;
    }

    public CrumbSource getParent() {
        return parent;
    }

    public String getName(BreadcrumbContext context) {
        return name;
    }

    public Map<String, String> getParameters(BreadcrumbContext context) {
        return params;
    }
}
