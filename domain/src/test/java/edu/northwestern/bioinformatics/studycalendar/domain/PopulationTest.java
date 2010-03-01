package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class PopulationTest extends TestCase {
    private Study study = new Study();
    private final Set<Population> populations = new HashSet<Population>();

    public void testNaturalOrderIsByName() throws Exception {
        Population w = Fixtures.createNamedInstance("Women", Population.class);
        Population m = Fixtures.createNamedInstance("men", Population.class);

        assertPositive(w.compareTo(m));
        assertNegative(m.compareTo(w));
    }

    public void testClone() throws Exception {
        Population population = new Population();
        population.setName("population");
        population.setStudy(study);
        study.addPopulation(population);
        Population clone = population.clone();


        assertNotSame("Clone is same", clone, population);
        assertEquals("must copy the name", clone.getName(), population.getName());
        assertNotSame("population not deep-cloned", clone, study.getPopulations().iterator().next());
        assertNull("parent study not cleared from cloned population", clone.getStudy());
    }

    public void testfindMatchingPopulationByAbbreviation() throws Exception {
        Population population = new Population();
        population.setName("population");
        population.setAbbreviation("p1");
        population.setStudy(study);
        study.addPopulation(population);

        populations.add(population);

        assertNull("must not find any population", Population.findMatchingPopulationByAbbreviation(populations, null));

        assertNotNull("must find population", Population.findMatchingPopulationByAbbreviation(populations, population));
        Population anotherPopulation = new Population();
        assertNull("must not find population", Population.findMatchingPopulationByAbbreviation(populations, anotherPopulation));
        anotherPopulation.setAbbreviation("p1");
        assertNotNull("must  find population", Population.findMatchingPopulationByAbbreviation(populations, anotherPopulation));

    }
    
    public void testIsDetachedWhenStudyNotPresent() throws Exception {
        Population population = new Population();
        assertTrue("Population is attached to study ", population.isDetached());
    }

    public void testIsDetachedWhenStudyIsPresent() throws Exception {
        Population population = new Population();
        population.setStudy(study);
        assertFalse("Population is detached from study ", population.isDetached());
    }

    public void testTransientClone() throws Exception {
        Population p = Fixtures.createPopulation("abbr", "name");

        Population clone = p.transientClone();
        assertNotNull(clone.getName());
        assertNotNull(clone.getAbbreviation());
        assertTrue(clone.isMemoryOnly());
    }

    public void testDeepEqualsForDifferentName() throws Exception {
        Population p1 = Fixtures.createPopulation("N", "name1");
        Population p2 = Fixtures.createPopulation("N", "name2");
        Differences differences = p1.deepEquals(p2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Population is not different", "Population name name1 differs to name2", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentAbbreviation() throws Exception {
        Population p1 = Fixtures.createPopulation("N1", "name");
        Population p2 = Fixtures.createPopulation("N2", "name");
        Differences differences = p1.deepEquals(p2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Population is not different", "Population abbreviation N1 differs to N2", differences.getMessages().get(0));
    }
}
