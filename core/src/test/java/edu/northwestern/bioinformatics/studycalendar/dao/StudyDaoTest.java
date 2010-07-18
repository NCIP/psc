package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class StudyDaoTest extends ContextDaoTestCase<StudyDao> {
    public void testGetById() throws Exception {
        Study study = getDao().getById(-100);
        assertIsTestStudy100(study);
    }

    public void testGetByGridId() throws Exception {
        Study actual = getDao().getByGridId("long-GUID-string");
        assertIsTestStudy100(actual);
    }

    public void testGetByGridIdByTemplate() throws Exception {
        Study actual = getDao().getByGridId(Fixtures.setGridId("long-GUID-string", new Study()));
        assertIsTestStudy100(actual);
    }

    public void testGetByAssignedIdentifier() throws Exception {
        Study actual = getDao().getByAssignedIdentifier("NCI-IS-WATCHING");
        assertIsTestStudy100(actual);
    }

    public void testGetByAssignedIdentifiers() throws Exception {
        List<Study> actual = getDao().getByAssignedIdentifiers(
            Arrays.asList("NCI-IS-WATCHING", "another nci study"));
        assertEquals("Wrong number of results", 2, actual.size());
        assertIsTestStudy100(actual.get(0));
        assertEquals("Wrong second study", new Integer(-102), actual.get(1).getId());
    }

    public void testSearchByName() throws Exception {
        List<Study> studies = getDao().searchStudiesByStudyName("NCi");
        assertEquals("there must be 2 studies", 2, studies.size());
        for (Study study : studies) {
            assertTrue("study must have assigned identifier matching %nci% string",
                study.getName().toLowerCase().contains("nci"));
        }
        Collection<Integer> ids = DomainObjectTools.collectIds(studies);
        assertContains("Wrong study found", ids, -100);
        assertContains("Wrong study found", ids, -102);

        // now search with another string such that no study maatches for the given serach string
        String identifierWhichDoesNotExists = "identifier which does not exists";
        studies = getDao().searchStudiesByStudyName(identifierWhichDoesNotExists);
        assertEquals("there must be 3 studies", 3, studies.size());
        for (Study study : studies) {
            assertTrue("study must not have assigned identifier matching %nci identifier which does not exists% string",
                !study.getName().toLowerCase().contains(identifierWhichDoesNotExists));
        }
        ids = DomainObjectTools.collectIds(studies);
        assertContains("Wrong study found", ids, -100);
        assertContains("Wrong study found", ids, -101);
        assertContains("Wrong study found", ids, -102);
    }

    public void testLoadAmendments() throws Exception {
        Study study = getDao().getById(-100);
        assertNotNull("Missing current amendment", study.getAmendment());
        assertNotNull("Missing current amendment is default (not loaded)", study.getAmendment().getId());
        assertEquals("Wrong current amendment", -45, (int) study.getAmendment().getId());
        assertNotNull("Missing dev amendment", study.getDevelopmentAmendment());
        assertEquals("Wrong dev amendment", -55, (int) study.getDevelopmentAmendment().getId());
    }

    public void testLoadPopulations() throws Exception {
        Study loaded = getDao().getById(-100);
        assertEquals("Wrong number of populations", 2, loaded.getPopulations().size());
        Collection<Integer> ids = DomainObjectTools.collectIds(loaded.getPopulations());
        assertContains("Missing expected population", ids, -64);
        assertContains("Missing expected population", ids, -96);
    }

    public void testLoadSecondaryIdentifiers() throws Exception {
        Study loaded = getDao().getById(-100);

        assertEquals("Wrong number of secondary identifiers: " + loaded.getSecondaryIdentifiers(), 3,
            loaded.getSecondaryIdentifiers().size());
        Iterator<StudySecondaryIdentifier> it = loaded.getSecondaryIdentifiers().iterator();
        assertSecondaryIdentifier("Bad first ident", "NCT", "NCT100", it.next());
        assertSecondaryIdentifier("Bad second ident", "Organization", "ECOG-100", it.next());
        assertSecondaryIdentifier("Bad third ident", "Organization", "SWOG-100", it.next());
    }

    public void testDeleteSecondaryIdent() throws Exception {
        {
            Study loaded = getDao().getById(-100);
            Iterator<StudySecondaryIdentifier> it = loaded.getSecondaryIdentifiers().iterator();
            it.next();
            StudySecondaryIdentifier identToRemove = it.next();
            assertSecondaryIdentifier("Test setup failure", "Organization", "ECOG-100", identToRemove);
            loaded.getSecondaryIdentifiers().remove(identToRemove);
            getDao().save(loaded);
        }

        interruptSession();

        {
            Study reloaded = getDao().getById(-100);
            assertEquals("Wrong number of idents after delete", 2,
                reloaded.getSecondaryIdentifiers().size());
            Iterator<StudySecondaryIdentifier> it = reloaded.getSecondaryIdentifiers().iterator();
            assertSecondaryIdentifier("Wrong remaining ident", "NCT", "NCT100", it.next());
            assertSecondaryIdentifier("Wrong remaining ident", "Organization", "SWOG-100", it.next());
        }
    }
    
    public void testAddSecondaryIdent() throws Exception {
        {
            Study loaded = getDao().getById(-100);
            Fixtures.addSecondaryIdentifier(loaded, "A", "One");
            getDao().save(loaded);
        }

        interruptSession();

        {
            Study reloaded = getDao().getById(-100);
            assertEquals("Wrong number of identifiers", 4, reloaded.getSecondaryIdentifiers().size());
            assertSecondaryIdentifier("Wrong new ident", "A", "One",
                reloaded.getSecondaryIdentifiers().iterator().next());
        }
    }

    private void assertSecondaryIdentifier(
        String message, String expectedType, String expectedIdent, StudySecondaryIdentifier actual
    ) {
        assertEquals(message + ": wrong type",  expectedType,  actual.getType());
        assertEquals(message + ": wrong value", expectedIdent, actual.getValue());
    }

    public void testGetAll() throws Exception {
        List<Study> actual = getDao().getAll();
        assertEquals(3, actual.size());
        Collection<Integer> ids = DomainObjectTools.collectIds(actual);
        assertContains("Wrong study found", ids, -100);
        assertContains("Wrong study found", ids, -101);
        assertContains("Wrong study found", ids, -102);
    }

    public void testSaveNewStudy() throws Exception {
        Integer savedId;
        {
            Study study = new Study();
            study.setAssignedIdentifier("New study");
            study.setLongTitle("New study");
            getDao().save(study);
            savedId = study.getId();
            assertNotNull("The saved study didn't get an id", savedId);
        }

        interruptSession();

        {
            Study loaded = getDao().getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong name", "New study", loaded.getName());
            assertNotNull("Grid ID not automatically added", loaded.getGridId());
        }
    }

    public void testSaveNewStudyWithPopulation() throws Exception {
        Integer savedId;
        {
            Study study = new Study();
            study.setAssignedIdentifier("New study");
            study.setLongTitle("New study");
            Population population = new Population();
            population.setName("pop1");
            population.setAbbreviation("p1");
            study.addPopulation(population);
            getDao().save(study);
            savedId = study.getId();
            assertNotNull("The saved study didn't get an id", savedId);
            assertFalse("must load populations", study.getPopulations().isEmpty());

        }

        interruptSession();

        {
            Study loaded = getDao().getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong name", "New study", loaded.getName());
            assertNotNull("Grid ID not automatically added", loaded.getGridId());
            Population population = loaded.getPopulations().iterator().next();
            assertNotNull("Could not reload study with id " + population);
            assertNotNull("Grid ID not automatically added", population.getGridId());
            assertEquals("Wrong name", "pop1", population.getName());
        }
    }

    public void testGetStudySubjectAssigments() throws Exception {
        List<StudySubjectAssignment> actual = getDao().getAssignmentsForStudy(-100);
        assertEquals("Wrong number of assigments", 2, actual.size());
        List<Integer> ids = new ArrayList<Integer>(DomainObjectTools.collectIds(actual));

        assertContains("Missing expected assignment", ids, -10);
        assertContains("Missing expected assignment", ids, -11);
        assertEquals("Assignments in wrong order", -10, (int) ids.get(0));
        assertEquals("Assignments in wrong order", -11, (int) ids.get(1));
    }

    private void assertIsTestStudy100(final Study actual) {
        assertNotNull("Could not locate", actual);
        assertEquals("Wrong id", -100, (int) actual.getId());
        assertEquals("Wrong grid ID", "long-GUID-string", actual.getGridId());
        assertEquals("Wrong protocol auth id", "NCI-IS-WATCHING", actual.getAssignedIdentifier());
        assertEquals("Wrong provider", "NCT", actual.getProvider());
        assertDayOfDate("Wrong last refresh", 2007, Calendar.MARCH, 29, actual.getLastRefresh());
        assertTimeOfDate("Wrong last refresh", 13, 45, 7, 0, actual.getLastRefresh());
        assertEquals("Wrong managing site count", 1, actual.getManagingSites().size());
        Site actualManager = actual.getManagingSites().iterator().next();
        assertEquals("Wrong managing site", -40, (int) actualManager.getId());
        assertSame("Managing site does not have link back to study", actual,
            actualManager.getManagedStudies().iterator().next());
    }

    public void testDeleteStudy() throws Exception {
        getDao().delete(getDao().getById(-100));
        // no exceptions
        assertNull(getDao().getById(-100));
    }
}
