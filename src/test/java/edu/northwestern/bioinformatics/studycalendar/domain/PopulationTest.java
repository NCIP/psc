package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class PopulationTest extends StudyCalendarTestCase {
    private Population population;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        population = new Population();
    }

    public void testSetStudyMaintainsBidirectionality() throws Exception {
        Study s = new Study();
        assertEquals("Test setup failure", 0, s.getPopulations().size());
        population.setStudy(s);
        assertSame(s, population.getStudy());
        assertEquals("Not added", 1, s.getPopulations().size());
    }
    
    public void testSetStudyIsNullSafe() throws Exception {
        population.setStudy(null);
        // no exceptions
    }
}
