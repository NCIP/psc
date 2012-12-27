/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class PscStudyMappingTest extends StudyCalendarTestCase {
    private PscStudyMapping mapping;
    private StudyDao studyDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerMockFor(StudyDao.class);

        mapping = new PscStudyMapping();
        mapping.setStudyDao(studyDao);
    }

    public void testAStudyIsAnInstance() throws Exception {
        assertTrue(mapping.isInstance(new Study()));
    }

    public void testAPlannedCalendarIsNotAnInstance() throws Exception {
        assertFalse(mapping.isInstance(new PlannedCalendar()));
    }

    // TODO: this is not entirely going to work
    public void testSharedIdentityIsAssignedIdentity() throws Exception {
        assertEquals("NU 7578", mapping.getSharedIdentity(Fixtures.createBasicTemplate("NU 7578")));
    }

    public void testGetApplicationInstancesUsesTheDao() throws Exception {
        List<String> expectedIdentifiers = Arrays.asList("NU 5202", "NU 4221");
        List<Study> expectedStudies = Arrays.asList(
            Fixtures.createBasicTemplate("NU 5202"), Fixtures.createBasicTemplate("NU 4221"));
        expect(studyDao.getByAssignedIdentifiers(expectedIdentifiers)).andReturn(expectedStudies);

        replayMocks();
        assertSame(expectedStudies, mapping.getApplicationInstances(expectedIdentifiers));
        verifyMocks();
    }
}
