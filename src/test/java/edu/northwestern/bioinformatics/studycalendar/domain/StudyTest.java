package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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

    public void testPushAmendment() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        study.pushAmendment(a);
        assertSame(a, study.getAmendment());
        Amendment b = new Amendment();
        study.pushAmendment(b);
        assertSame(b, study.getAmendment());
        assertSame(a, b.getPreviousAmendment());
    }

    public void testGetAmendmentsList() throws Exception {
        study.pushAmendment(new Amendment("A"));
        study.pushAmendment(new Amendment("B"));
        study.pushAmendment(new Amendment("C"));

        List<Amendment> actual = study.getAmendmentsList();
        assertEquals("Wrong number of amendments", 3, actual.size());
        assertEquals("Most recent not first", "C", actual.get(0).getName());
        assertEquals("Amendments not in order", "B", actual.get(1).getName());
        assertEquals("Amendments not in order", "A", actual.get(2).getName());
    }

    public void testAmendmentsListIsImmutableView() throws Exception {
        study.pushAmendment(new Amendment("A"));
        study.pushAmendment(new Amendment("B"));
        study.pushAmendment(new Amendment("C"));

        List<Amendment> actual = study.getAmendmentsList();
        try {
            actual.remove(1);
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            // good
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

    public void testGetStudySiteForNonAssociatedSite() throws Exception {
        Site s4 = Fixtures.createNamedInstance("S4", Site.class);
        study.addSite(Fixtures.createNamedInstance("S2", Site.class));
        study.addSite(Fixtures.createNamedInstance("S3", Site.class));

        assertNull(study.getStudySite(s4));
    }

    public void testGetStudySiteForAssociatedSite() throws Exception {
        Site s3 = Fixtures.createNamedInstance("S3", Site.class);
        study.addSite(Fixtures.createNamedInstance("S2", Site.class));
        study.addSite(s3);

        assertNotNull(study.getStudySite(s3));
        assertSame(study, study.getStudySite(s3).getStudy());
        assertSame(s3, study.getStudySite(s3).getSite());
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

    public void testAddPopulation() throws Exception {
        assertEquals("Test setup failure", 0, study.getPopulations().size());
        Population p = new Population();
        study.addPopulation(p);
        assertEquals("Not added", 1, study.getPopulations().size());
        assertSame("Bidirectionality not maintained", study, p.getStudy());
    }

    public void testCloneWithNoCalendarWorks() throws Exception {
        assertNull("Test setup failure", study.getPlannedCalendar());

        Study clone = study.clone();
        assertNotSame("Clone is same", study, clone);
    }

    public void testClone() throws Exception {
        study.setPlannedCalendar(new PlannedCalendar());
        Study clone = study.clone();

        assertNotSame("Clone is same", study, clone);
        assertNotSame("Planned calendar not deep-cloned", study.getPlannedCalendar(), clone.getPlannedCalendar());
        assertEquals("Cloned planned calendar does not refer to study clone", clone, clone.getPlannedCalendar().getStudy());
    }

    public void testTransientCloneIncludesTransientPlannedCalendar() throws Exception {
        study.setPlannedCalendar(new PlannedCalendar());
        Study clone = study.transientClone();

        assertFalse("Original study marked mem-only", study.isMemoryOnly());
        assertTrue("Clone not transient", clone.isMemoryOnly());
        assertNotNull("Planned calendar not in clone", clone.getPlannedCalendar());
        assertEquals("Cloned planned calendar does not refer to study clone", clone, clone.getPlannedCalendar().getStudy());
        assertTrue("Cloned calendar not transient", clone.getPlannedCalendar().isMemoryOnly());
    }

    public void testLastModifiedDate() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        a.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(a);

        Amendment b = new Amendment();
        b.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 21));
        study.pushAmendment(b);

        Amendment c = new Amendment();
        c.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 24));
        study.pushAmendment(c);
        assertEquals(DateUtils.createDate(2007, Calendar.OCTOBER, 24), study.getLastModifiedDate());
    }

    public void testLastModifiedDateWhenStudyIsAmended() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        a.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(a);

        Amendment b = new Amendment();
        b.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 23));
        study.setDevelopmentAmendment(b);

        assertEquals(DateUtils.createDate(2007, Calendar.OCTOBER, 23), study.getLastModifiedDate());
    }

    public void testLastModifiedDateForNewlyCreatedStudy() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        a.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 19));

        study.setDevelopmentAmendment(a);
        assertSame(a, study.getDevelopmentAmendment());

        assertEquals(DateUtils.createDate(2007, Calendar.OCTOBER, 19), study.getLastModifiedDate());
    }
}
