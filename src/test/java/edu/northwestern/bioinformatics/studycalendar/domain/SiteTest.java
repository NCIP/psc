package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class SiteTest extends StudyCalendarTestCase {
    private Site site;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        site = new Site();
    }

    public void testGetStudySiteForUnaffiliatedStudy() throws Exception {
        Study s2 = Fixtures.createSingleEpochStudy("S2", "E");
        site.addStudySite(Fixtures.createStudySite(Fixtures.createSingleEpochStudy("S3", "E"), site));
        site.addStudySite(Fixtures.createStudySite(Fixtures.createSingleEpochStudy("S4", "E"), site));

        assertNull(site.getStudySite(s2));
    }

    public void testAssignedIdentifierDefaultsToName() throws Exception {
        site.setName("arb");
        site.setAssignedIdentifier(null);
        assertEquals("arb", site.getAssignedIdentifier());
    }

    public void testExplicitAssignedIdentifierTrumps() throws Exception {
        site.setName("foo");
        site.setAssignedIdentifier("bar");
        assertEquals("bar", site.getAssignedIdentifier());
    }
    
    public void testGetStudySiteForAffiliatedStudy() throws Exception {
        Study s3 = Fixtures.createSingleEpochStudy("S3", "E");
        site.addStudySite(Fixtures.createStudySite(s3, site));
        site.addStudySite(Fixtures.createStudySite(Fixtures.createSingleEpochStudy("S4", "E"), site));

        assertNotNull(site.getStudySite(s3));
        assertSame(s3, site.getStudySite(s3).getStudy());
        assertSame(site, site.getStudySite(s3).getSite());
    }
}
