/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MockSiteProviderTest extends TestCase {
    private MockSiteProvider provider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Map<String, String> sites = new HashMap<String, String>();
        sites.put("IL036", "Northwestern University Robert H. Lurie Comprehensive Cancer Center");
        sites.put("MN026", "Mayo Clinic Cancer Center");

        provider = new MockSiteProvider();
        provider.setSites(sites);
    }

    public void testGetSiteComesFromMap() throws Exception {
        Site actual = provider.getSites(Arrays.asList("MN026")).get(0);
        assertEquals("Wrong ident", "MN026", actual.getAssignedIdentifier());
        assertEquals("Wrong name", "Mayo Clinic Cancer Center", actual.getName());
    }

    public void testGetMultipleSites() throws Exception {
        List<Site> actual = provider.getSites(Arrays.asList("IL036", "UN000", "MN026"));
        assertEquals("Wrong number of results", 3, actual.size());
        assertEquals("Wrong first site",
            "Northwestern University Robert H. Lurie Comprehensive Cancer Center",
            actual.get(0).getName());
        assertNull("Should be no match for second site", actual.get(1));
        assertEquals("Wrong third site", "Mayo Clinic Cancer Center", actual.get(2).getName());
    }

    public void testGetUnknownSiteIsNull() throws Exception {
        assertNull(provider.getSites(Arrays.asList("IL")).get(0));
    }

    public void testSearchWithMultipleHits() throws Exception {
        List<Site> actual = provider.search("Cancer");
        assertEquals("Wrong number of hits", 2, actual.size());
    }

    public void testSearchMatchesNames() throws Exception {
        List<Site> actual = provider.search("Rob");
        assertEquals("Wrong number of hits", 1, actual.size());
        assertEquals("Wrong site", "IL036", actual.get(0).getAssignedIdentifier());
    }

    public void testSearchReturnsFullObjects() throws Exception {
        List<Site> actual = provider.search("Mayo");
        assertEquals("Wrong number of hits", 1, actual.size());
        assertEquals("Wrong site", "MN026", actual.get(0).getAssignedIdentifier());
        assertEquals("Mayo Clinic Cancer Center", actual.get(0).getName());
    }

    @SuppressWarnings({"ConstantConditions"})
    public void testIsRefreshable() throws Exception {
        assertTrue(provider instanceof RefreshableProvider);
    }

    public void testRefreshesEveryFifteenSeconds() throws Exception {
        assertEquals(15, (int) provider.getRefreshInterval());
    }
}
