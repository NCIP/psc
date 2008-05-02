package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

/**
 * @author Rhett Sutphin
*/
class TestCrumbSource implements CrumbSource {
    private Crumb crumb;

    public TestCrumbSource(Crumb crumb) {
        this.crumb = crumb;
    }

    public Crumb getCrumb() {
        return crumb;
    }
}
