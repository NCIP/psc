package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertContains;

/**
 * @author Rhett Sutphin
 */
public class StudyTest extends TestCase {
    private Study study = new Study();

    public void testGetSites() throws Exception {
        List<Site> expectedSites = Arrays.asList(
                createNamedInstance("Site 1", Site.class),
                createNamedInstance("Site 3", Site.class),
                createNamedInstance("Site 2", Site.class)
        );
        for (Site site : expectedSites) {
            createStudySite(study, site);
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
        Site s4 = createNamedInstance("S4", Site.class);
        study.addSite(createNamedInstance("S2", Site.class));
        study.addSite(createNamedInstance("S3", Site.class));

        assertNull(study.getStudySite(s4));
    }

    public void testGetStudySiteForAssociatedSite() throws Exception {
        Site s3 = createNamedInstance("S3", Site.class);
        study.addSite(createNamedInstance("S2", Site.class));
        study.addSite(s3);

        assertNotNull(study.getStudySite(s3));
        assertSame(study, study.getStudySite(s3).getStudy());
        assertSame(s3, study.getStudySite(s3).getSite());
    }

    public void testBasicAddSite() throws Exception {
        Site expectedSite = createNamedInstance("Site 1", Site.class);
        study.addSite(expectedSite);
        assertEquals("No studySite added", 1, study.getStudySites().size());
        assertSame("StudySite added not for passed site", expectedSite, study.getStudySites().get(0).getSite());
    }

    public void testAddSiteWhenExists() throws Exception {
        Site expectedSite = createNamedInstance("Site 1", Site.class);
        createStudySite(study, expectedSite);
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

    public void testCloneShouldCopyBasicProperty() throws Exception {
        study.setPlannedCalendar(new PlannedCalendar());
        study.setAssignedIdentifier("ECOG 0123");
        study.setName("ECOG Study");
        Study clone = study.clone();

        assertNotSame("Clone is same", study, clone);
        assertNotSame("Planned calendar not deep-cloned", study.getPlannedCalendar(), clone.getPlannedCalendar());

        assertEquals("Cloned planned calendar does not refer to study clone", clone, clone.getPlannedCalendar().getStudy());
        assertEquals("Cloned should coy the basic properties", clone.getName(), study.getName());
        assertEquals("Cloned should coy the basic properties", clone.getAssignedIdentifier(), study.getAssignedIdentifier());
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

    public void testCloneWithPopulations() throws Exception {
        Population p1 = createPopulation("abbreviation1", "name1");
        Population p2 = createPopulation("abbreviation2", "name2");
        Set<Population> populations = new HashSet<Population>();
        populations.add(p1);
        populations.add(p2);
        study.setPopulations(populations);
        Study clone = study.clone();

        assertFalse("Original study marked mem-only", study.isMemoryOnly());
        assertFalse("Clone is transient", clone.isMemoryOnly());
        Set<Population> clonedPopulations = clone.getPopulations();
        Population[] populationsArray = new Population[2];
        populationsArray = clonedPopulations.toArray(populationsArray);

        assertNotSame("Clone is same", study, clone);
        assertNotSame("Populations not deep-cloned", study.getPopulations(), clone.getPopulations());

        assertNotNull("First population not in clone", populationsArray[0]);
        assertNotNull("Second population not in clone", populationsArray[1]);
        assertEquals("Cloned first population does not refer to study clone", clone, populationsArray[0].getStudy());
        assertEquals("Cloned second population does not refer to study clone", clone, populationsArray[1].getStudy());
    }

    public void testCloneDeepClonesAmendment() throws Exception {
        Study src = createReleasedTemplate();
        Study clone = src.clone();
        assertNotSame("Amendments not cloned",
            src.getAmendment(), clone.getAmendment());
    }

    public void testCloneDeepClonesDevAmendment() throws Exception {
        Study src = createInDevelopmentTemplate("DC");
        Study clone = src.clone();
        assertNotSame("Dev amendment not cloned",
            src.getDevelopmentAmendment(), clone.getDevelopmentAmendment());
    }

    public void testTransientCloneIncludesTransientAmendment() throws Exception {
        Study clone = createReleasedTemplate().transientClone();
        assertTrue(clone.getAmendment().isMemoryOnly());
    }

    public void testTransientCloneIncludesTransientDevAmendment() throws Exception {
        Study clone = createInDevelopmentTemplate("DC").transientClone();
        assertTrue(clone.getDevelopmentAmendment().isMemoryOnly());
    }

    public void testCloneDeepClonesSecondaryIdentifiers() throws Exception {
        StudySecondaryIdentifier original = addSecondaryIdentifier(study, "E", "1");
        Study clone = study.clone();
        assertNotSame("Not cloned", original, clone.getSecondaryIdentifiers().first());
    }

    public void testLastModifiedDate() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        a.setReleasedDate(DateTools.createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(a);

        Amendment b = new Amendment();
        b.setReleasedDate(DateTools.createDate(2007, Calendar.OCTOBER, 21));
        study.pushAmendment(b);

        Amendment c = new Amendment();
        c.setReleasedDate(DateTools.createDate(2007, Calendar.OCTOBER, 24));
        study.pushAmendment(c);
        assertEquals(DateTools.createDate(2007, Calendar.OCTOBER, 24), study.getLastModifiedDate());
    }

    public void testLastModifiedDateWhenStudyIsAmended() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        a.setReleasedDate(DateTools.createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(a);

        Amendment b = new Amendment();
        b.setReleasedDate(DateTools.createDate(2007, Calendar.OCTOBER, 23));
        study.setDevelopmentAmendment(b);

        assertEquals(DateTools.createDate(2007, Calendar.OCTOBER, 23), study.getLastModifiedDate());
    }

    public void testLastModifiedDateForNewlyCreatedStudy() throws Exception {
        assertNull(study.getAmendment());
        Amendment a = new Amendment();
        a.setReleasedDate(DateTools.createDate(2007, Calendar.OCTOBER, 19));

        study.setDevelopmentAmendment(a);
        assertSame(a, study.getDevelopmentAmendment());

        assertEquals(DateTools.createDate(2007, Calendar.OCTOBER, 19), study.getLastModifiedDate());
    }

    public void testIsDetached() throws Exception {
        assertFalse("Study is detached from any element ", study.isDetached());
    }

    public void testAddSecondaryIdentifierAddsTheIdentifier() throws Exception {
        StudySecondaryIdentifier ident = createStudyIdentifier("A", "1");
        assertEquals(0, study.getSecondaryIdentifiers().size());
        study.addSecondaryIdentifier(ident);
        assertEquals(1, study.getSecondaryIdentifiers().size());
        assertSame(ident, study.getSecondaryIdentifiers().iterator().next());
    }

    public void testAddSecondaryIdentifierCreatesLink() throws Exception {
        StudySecondaryIdentifier ident = createStudyIdentifier("A", "1");
        assertNull(ident.getStudy());
        study.addSecondaryIdentifier(ident);
        assertSame(study, ident.getStudy());
    }

    public void testGetSecondaryIdentifierIsNullForUnknownType() throws Exception {
        assertNull(study.getSecondaryIdentifierValue("some id"));
    }
    
    public void testGetSecondaryIdentifierReturnsFirstOfType() throws Exception {
        addSecondaryIdentifier(study, "foo", "A");
        addSecondaryIdentifier(study, "foo", "B");

        assertEquals("A", study.getSecondaryIdentifierValue("foo"));
    }

    public void testIsNotManagedWithNoManagers() throws Exception {
        assertFalse("Should not be managed", new Study().isManaged());
    }

    public void testIsManagedWithOneManagingSite() throws Exception {
        Site b = createSite("B");
        study.addManagingSite(b);
        assertTrue("Should be managed", study.isManaged());
    }

    public void testAddManagingSiteAddsTheSite() throws Exception {
        Site q = createSite("Q");
        study.addManagingSite(q);
        assertTrue("Missing new site", study.getManagingSites().contains(q));
    }

    public void testAddManagingSiteMaintainsBidirectionalRelationship() throws Exception {
        Site q = createSite("Q");
        study.addManagingSite(q);
        assertTrue("Site missing new study", q.getManagedStudies().contains(study));
    }

    public void testRemoveManagingSiteRemovesTheSite() throws Exception {
        Site q = createSite("Q");
        study.addManagingSite(q);
        study.removeManagingSite(q);
        assertFalse("Site still present", study.getManagingSites().contains(q));
    }

    public void testRemoveManagingSiteRemovesBidirectionalRelationship() throws Exception {
        Site q = createSite("Q");
        study.addManagingSite(q);
        study.removeManagingSite(q);
        assertFalse("Site still has study", q.getManagedStudies().contains(study));
    }

    public void testRemoveNonManagingSiteDoesNothing() throws Exception {
        Site q = createSite("Q");
        study.removeManagingSite(q);
        // no exception
    }

    public void testReleasedDisplayNameIncludesAmendmentIfAmended() throws Exception {
        Study amended = createReleasedTemplate("Etc");
        Amendment cur = createAmendment("No", DateTools.createDate(2007, Calendar.JUNE, 3));
        cur.setPreviousAmendment(amended.getAmendment());
        amended.setAmendment(cur);
        assertEquals("Etc [2007-06-03 (No)]", amended.getReleasedDisplayName());
    }

    public void testReleasedDisplayNameDoesNotIncludesAmendmentIfInitial() throws Exception {
        assertEquals("Etc", createReleasedTemplate("Etc").getReleasedDisplayName());
    }

    public void testReleasedDisplayNameIsNullIfNeverReleased() throws Exception {
        assertNull(study.getReleasedDisplayName());
    }

    public void testDevelopmentDisplayNameIncludesDevelopmentAmendmentNameWhenPreviouslyReleased() throws Exception {
        study.setAssignedIdentifier("Etc");
        study.setDevelopmentAmendment(new Amendment());
        study.getDevelopmentAmendment().setName(null);
        study.getDevelopmentAmendment().setDate(DateTools.createDate(2006, Calendar.JULY, 9));
        study.setAmendment(createAmendments(DateTools.createDate(2006, Calendar.MARCH, 3)));

        assertEquals("Etc [2006-07-09]", study.getDevelopmentDisplayName());
    }

    public void testDevelopmentDisplayNameDoesNotIncludeDevAmendmentNameWhenInitial() throws Exception {
        study.setAssignedIdentifier("Etc");
        study.setDevelopmentAmendment(new Amendment());
        assertEquals("Etc", study.getDevelopmentDisplayName());
    }

    public void testDevelopmentDisplayNameIsNullWhenNotInDevelopment() throws Exception {
        study.setAssignedIdentifier("Etc");
        assertNull(study.getDevelopmentDisplayName());
    }
}
