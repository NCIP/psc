/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MockStudyProviderTest extends TestCase {
    private Map<String, Study> mockStudies = new MapBuilder<String, Study>().
        put("A", createStudy("A")).
        put("B", createStudy("B")).
        put("C", createStudy("C")).
        toMap();
    private MockStudyProvider provider;

    public void setUp() throws Exception {
        super.setUp();
        provider = new MockStudyProvider();
        provider.setStudies(mockStudies);
    }

    public void testGetStudiesFindsCorrespondingInstanceByNctId() throws Exception {
        Study param = new Study();
        addSecondaryIdentifier(param, "nct", "B");

        Study found = provider.getStudies(Arrays.asList(param)).get(0);
        assertEquals("Wrong study found", "B", found.getAssignedIdentifier());
    }

    public void testGetStudiesReturnsNullForUnknownStudy() throws Exception {
        Study param = new Study();
        addSecondaryIdentifier(param, "nct", "D");

        Study found = provider.getStudies(Arrays.asList(param)).get(0);
        assertNull(found);
    }

    public void testGetStudiesReturnsNullForNoNctId() throws Exception {
        Study param = new Study();
        addSecondaryIdentifier(param, "secondary", "ECOG-B");

        Study found = provider.getStudies(Arrays.asList(param)).get(0);
        assertNull(found);
    }

    public void testGetStudiesReturnsClones() throws Exception {
        Study param = new Study();
        addSecondaryIdentifier(param, "nct", "B");

        Study found = provider.getStudies(Arrays.asList(param)).get(0);
        assertNotSame("Study not cloned", mockStudies.get("B"), found);
    }

    public void testSearchIncludesAllIdentifiers() throws Exception {
        List<Study> found = provider.search("ECOG-C");
        assertEquals("Wrong number of studies found", 1, found.size());
        assertEquals("Wrong study", "C", found.get(0).getName());
    }

    public void testSearchIncludesLongTitle() throws Exception {
        List<Study> found = provider.search("named B");
        assertEquals("Wrong number of studies found", 1, found.size());
        assertEquals("Wrong study", "B", found.get(0).getName());
    }

    public void testSearchReturnsClones() throws Exception {
        List<Study> found = provider.search("B");
        assertNotSame("Study not cloned", mockStudies.get("B"), found.get(0));
    }

    public void testDetectAlignsByNctIdent() throws Exception {
        Study param = new Study();
        addSecondaryIdentifier(param, "nct", "8");
        List<Study> studies = Arrays.asList(
            createStudy("7"), createStudy("8"), createStudy("9")
        );

        Study actual = provider.detect(param, studies);
        assertSame(actual, studies.get(1));
    }

    public void testDetectReturnsNullForNotFound() throws Exception {
        Study param = new Study();
        addSecondaryIdentifier(param, "secondary", "ECOG-8");
        List<Study> studies = Arrays.asList(
            createStudy("7"), createStudy("8"), createStudy("9")
        );

        Study actual = provider.detect(param, studies);
        assertNull(actual);
    }

    @SuppressWarnings({"ConstantConditions"})
    public void testIsRefreshable() throws Exception {
        assertTrue(provider instanceof RefreshableProvider);
    }

    public void testRefreshesEveryFifteenSeconds() throws Exception {
        assertEquals(15, (int) provider.getRefreshInterval());
    }

    private Study createStudy(String nct) {
        Study s = new Study();
        s.setAssignedIdentifier(nct);
        addSecondaryIdentifier(s, "nct", nct);
        addSecondaryIdentifier(s, "secondary", "ECOG-" + nct);
        s.setLongTitle("A study named " + nct);
        return s;
    }

    private void addSecondaryIdentifier(Study s, String type, String value) {
        StudySecondaryIdentifier ident = new StudySecondaryIdentifier();
        ident.setType(type);
        ident.setValue(value);
        s.addSecondaryIdentifier(ident);
    }
}
