/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class MockStudySiteProviderTest extends TestCase {
    private MockStudySiteProvider provider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        List<MockStudySiteMapping> pairs = new LinkedList<MockStudySiteMapping>();
        pairs.add(new MockStudySiteMapping("NCT0100", "IL036"));
        pairs.add(new MockStudySiteMapping("NCT0101", "IL036"));
        pairs.add(new MockStudySiteMapping("NCT0102", "IL036"));
        pairs.add(new MockStudySiteMapping("NCT0100", "MN026"));
        pairs.add(new MockStudySiteMapping("NCT0200", "MN026"));

        provider = new MockStudySiteProvider();
        provider.setPairs(pairs);
    }

    public void testGetSitesForStudy() throws Exception {
        List<List<StudySite>> actual = provider.getAssociatedSites(Arrays.asList(
            createStudy("NCT0100"), createStudy("NCT0200")
        ));
        assertEquals("Should be one entry per study", 2, actual.size());
        assertEquals("Wrong number of associations for NCT0100", 2,      actual.get(0).size());
        assertSiteResponse("Wrong first response for NCT0100", "IL036",  actual.get(0).get(0));
        assertSiteResponse("Wrong second response for NCT0100", "MN026", actual.get(0).get(1));
        assertEquals("Wrong number of associations for NCT0200", 1,      actual.get(1).size());
        assertSiteResponse("Wrong first response for NCT0200", "MN026",  actual.get(1).get(0));
    }

    public void testGetSitesForUnknownStudyIsNull() throws Exception {
        List<List<StudySite>> actual = provider.getAssociatedSites(Arrays.asList(
            createStudy("NCT0300")
        ));

        assertEquals("Wrong number of responses", 1, actual.size());
        assertNull("Should be null for unknown study", actual.get(0));
    }

    public void testGetSitesForNoNctIdIsNull() throws Exception {
        List<List<StudySite>> actual = provider.getAssociatedSites(Arrays.asList(
            createReleasedTemplate()
        ));

        assertEquals("Wrong number of responses", 1, actual.size());
        assertNull("Should be null for unknown study", actual.get(0));
    }

    private void assertSiteResponse(String msg, String expectedSite, StudySite actual) {
        assertNull(msg + ": should have no study", actual.getStudy());
        assertEquals(msg + ": wrong site", expectedSite, actual.getSite().getAssignedIdentifier());
    }

    private Study createStudy(String nctIdent) {
        Study s = createNamedInstance(nctIdent, Study.class);
        addSecondaryIdentifier(s, "nct", nctIdent);
        s.setProvider(MockDataProviderTools.PROVIDER_TOKEN);
        return s;
    }

    public void testGetStudiesForSite() throws Exception {
        List<List<StudySite>> actual = provider.getAssociatedStudies(Arrays.asList(
            Fixtures.createSite("NU", "IL036"), Fixtures.createSite("Mayo", "MN026")
        ));

        assertEquals("Should be one entry per study", 2, actual.size());

        assertEquals("Wrong number of associations for IL036", 3, actual.get(0).size());
        assertStudyResponse("Wrong first response for IL036",  "NCT0100", actual.get(0).get(0));
        assertStudyResponse("Wrong second response for IL036", "NCT0101", actual.get(0).get(1));
        assertStudyResponse("Wrong third response for IL036",  "NCT0102", actual.get(0).get(2));

        assertEquals("Wrong number of associations for MN026", 2, actual.get(1).size());
        assertStudyResponse("Wrong first response for MN026",  "NCT0100", actual.get(1).get(0));
        assertStudyResponse("Wrong second response for MN026", "NCT0200", actual.get(1).get(1));
    }

    public void testGetStudiesForUnknownSiteIsNull() throws Exception {
        List<List<StudySite>> actual = provider.getAssociatedStudies(Arrays.asList(
            Fixtures.createSite("NU", "CA141")
        ));

        assertEquals("Wrong number of responses", 1, actual.size());
        assertNull("Should be null for unknown site", actual.get(0));
    }
    
    public void testGetStudiesForNoAssignedIdentIsNull() throws Exception {
        List<List<StudySite>> actual = provider.getAssociatedStudies(Arrays.asList(new Site()));

        assertEquals("Wrong number of responses", 1, actual.size());
        assertNull("Should be null for unknown site", actual.get(0));
    }

    private void assertStudyResponse(String msg, String expectedNct, StudySite actual) {
        assertNull(msg + ": should have no site", actual.getSite());
        assertEquals(msg + ": wrong study", expectedNct,
            actual.getStudy().getSecondaryIdentifierValue(MockDataProviderTools.KEY_STUDY_IDENTIFIER_TYPE));
    }
}
