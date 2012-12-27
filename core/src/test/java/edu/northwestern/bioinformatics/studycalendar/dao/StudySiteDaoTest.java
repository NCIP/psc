/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StudySiteDaoTest  extends ContextDaoTestCase<StudySiteDao> {
    private static final int ALL_STUDY_SITES_COUNT = 3;

    public void testGetById() throws Exception {
        StudySite studySite = getDao().getById(-300);

        assertEquals("Wrong Study", "Study A", studySite.getStudy().getName());
        
        assertEquals("Wrong number of approvals", 1, studySite.getAmendmentApprovals().size());
        assertEquals("Wrong approval", -310, (int) studySite.getAmendmentApprovals().get(0).getId());
    }

    public void testApprovalsLoadedInOrderByApprovalDate() throws Exception {
        StudySite ss = getDao().getById(-301);
        assertEquals("Wrong number of approvals", 3, ss.getAmendmentApprovals().size());
        assertEquals("First approval should be earliest", -1036, (int) ss.getAmendmentApprovals().get(0).getId());
        assertEquals("Wrong second approval", -1050, (int) ss.getAmendmentApprovals().get(1).getId());
        assertEquals("Last approval should be latest", -1048, (int) ss.getAmendmentApprovals().get(2).getId());
    }

    // further tests under getIntersections
    public void testGetIntersectionIdsForFoundIntersection() throws Exception {
        List<Integer> actual = getDao().getIntersectionIds(Arrays.asList(-100), Arrays.asList(-200));

        assertEquals("Wrong number of study sites IDs: " + actual, 1, actual.size());
        assertEquals("Wrong intersection", -300, (int) actual.get(0));
    }

    public void testGetIntersectionIdsForAll() throws Exception {
        List<Integer> actual = getDao().getIntersectionIds(null, null);

        assertNull("Should be for all (i.e., null): " + actual, actual);
    }

    public void testGetIntersectionsForFoundIntersection() throws Exception {
        List<StudySite> actual = getDao().getIntersections(Arrays.asList(-100), Arrays.asList(-200));

        assertEquals("Wrong number of study sites: " + actual, 1, actual.size());
        assertStudySitePresent(-300, actual);
    }

    public void testGetIntersectionsForNotFoundIntersection() throws Exception {
        List<StudySite> actual = getDao().getIntersections(Arrays.asList(-101), Arrays.asList(-200));

        assertEquals("Wrong number of study sites: " + actual, 0, actual.size());
    }

    public void testGetIntersectionsForAllStudies() throws Exception {
        List<StudySite> actual = getDao().getIntersections(null, Arrays.asList(-201));

        assertEquals("Wrong number of study sites: " + actual, 2, actual.size());
        assertStudySitePresent(-301, actual);
        assertStudySitePresent(-311, actual);
    }

    public void testGetIntersectionsForAllSites() throws Exception {
        List<StudySite> actual = getDao().getIntersections(Arrays.asList(-100), null);

        assertEquals("Wrong number of study sites: " + actual, 2, actual.size());
        assertStudySitePresent(-300, actual);
        assertStudySitePresent(-301, actual);
    }

    public void testGetIntersectionsForAllEverything() throws Exception {
        List<StudySite> actual = getDao().getIntersections(null, null);

        assertEquals("Wrong number of study sites: " + actual, ALL_STUDY_SITES_COUNT, actual.size());
    }

    private void assertStudySitePresent(int expectedId, Collection<StudySite> actual) {
        for (StudySite actualSS : actual) {
            if (actualSS.getId().equals(expectedId)) return;
        }
        fail("Missing study " + expectedId);
    }
}
