package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.Set;

public class UserRoleTest extends StudyCalendarTestCase {
    public void testRemoveStudySites() throws Exception {
        Study study0 = setId(1, createNamedInstance("Study A", Study.class));
        Study study1 = setId(2, createNamedInstance("Study B", Study.class));

        Site site = setId(1, createNamedInstance("Northwestern", Site.class));

        StudySite studySite0 = createStudySite(study0, site);
        StudySite studySite1 = createStudySite(study1, site);

        UserRole userRole = new UserRole();
        userRole.addStudySite(studySite0);
        userRole.addStudySite(studySite1);

        assertEquals("Wrong Study Site Size", 2, userRole.getStudySites().size());

        userRole.removeStudySite(studySite0);
        assertEquals("Wrong Study Site Size", study1.getName(), userRole.getStudySites().get(0).getStudy().getName());
    }
}
