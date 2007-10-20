package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class StudySiteTest extends StudyCalendarTestCase {
    private StudySite studySite;

    protected void setUp() throws Exception {
        super.setUp();
        studySite = new StudySite();
    }
    
    public void testUsedWhenUsed() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        studySite.getStudyParticipantAssignments().add(assignment);
        assertTrue(studySite.isUsed());
    }

    public void testUsedWhenNotUsed() throws Exception {
        assertFalse(studySite.isUsed());
    }

    public void testFindStudySite() throws Exception {
        Study study0 = setId(1, createNamedInstance("Study A", Study.class));
        Study study1 = setId(2, createNamedInstance("Study B", Study.class));

        Site site0 = setId(1, createNamedInstance("Site A", Site.class));
        Site site1 = setId(2, createNamedInstance("Site B", Site.class));

        StudySite studySite0 = Fixtures.createStudySite(study0, site0);
        StudySite studySite1 = Fixtures.createStudySite(study0, site1);
        StudySite studySite2 = Fixtures.createStudySite(study1, site0);
        StudySite studySite3 = Fixtures.createStudySite(study1, site1);

        StudySite actualStudySite = StudySite.findStudySite(study0, site1);
        assertEquals("Wrong Study Site", studySite1, actualStudySite);
        assertNotEquals("Study Site  should not be equal", studySite0, actualStudySite);
        assertNotEquals("Study Site  should not be equal", studySite2, actualStudySite);
        assertNotEquals("Study Site  should not be equal", studySite3, actualStudySite);
    }
}
