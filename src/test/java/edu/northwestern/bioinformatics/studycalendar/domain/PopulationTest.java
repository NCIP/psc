package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class PopulationTest extends StudyCalendarTestCase {
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
}
