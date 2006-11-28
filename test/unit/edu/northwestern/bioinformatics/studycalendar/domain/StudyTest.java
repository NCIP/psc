package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class StudyTest extends StudyCalendarTestCase {
    private Study study = new Study();

    public void testGetSites() throws Exception {
        List<Site> expectedSites = Arrays.asList(
            Fixtures.createNamedInstance("Site 1", Site.class),
            Fixtures.createNamedInstance("Site 3", Site.class),
            Fixtures.createNamedInstance("Site 2", Site.class)
        );
        for (Site site : expectedSites) {
            Fixtures.createStudySite(study, site);
        }

        List<Site> actualSites = study.getSites();
        assertEquals("Wrong number of sites returned", expectedSites.size(), actualSites.size());
        for (Site site : expectedSites) {
            assertContains(actualSites, site);
        }
    }
    
    public void testGetSitesWithNone() throws Exception {
        assertNotNull(study.getSites());
        assertEquals(0, study.getSites().size());
    }

    public void testGetSitesImmutable() throws Exception {
        List<Site> sites = study.getSites();
        try {
            sites.add(new Site());
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            // good
        }
    }

    public void testBasicAddSite() throws Exception {
        Site expectedSite = Fixtures.createNamedInstance("Site 1", Site.class);
        study.addSite(expectedSite);
        assertEquals("No studySite added", 1, study.getStudySites().size());
        assertSame("StudySite added not for passed site", expectedSite, study.getStudySites().get(0).getSite());
    }

    public void testAddSiteWhenExists() throws Exception {
        Site expectedSite = Fixtures.createNamedInstance("Site 1", Site.class);
        Fixtures.createStudySite(study, expectedSite);
        assertEquals("Test setup incorrect", 1, study.getStudySites().size());

        study.addSite(expectedSite);
        assertEquals("Extra studySite added", 1, study.getStudySites().size());
    }
}
