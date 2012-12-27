/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class PscSiteMappingTest extends StudyCalendarTestCase {
    private PscSiteMapping mapping;
    private SiteDao siteDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteDao = registerMockFor(SiteDao.class);

        mapping = new PscSiteMapping();
        mapping.setSiteDao(siteDao);
    }

    public void testASiteIsAnInstance() throws Exception {
        assertTrue(mapping.isInstance(new Site()));
    }

    public void testAStudyIsNotAnInstance() throws Exception {
        assertFalse(mapping.isInstance(new Study()));
    }

    // TODO: this is not entirely going to work
    public void testSharedIdentityIsAssignedIdentity() throws Exception {
        assertEquals("78", mapping.getSharedIdentity(Fixtures.createSite("Foom", "78")));
    }

    public void testGetApplicationInstancesUsesTheDao() throws Exception {
        List<String> expectedIdentifiers = Arrays.asList("2025", "1224");
        List<Site> expectedSites = Arrays.asList(
            Fixtures.createSite("A", "2025"), Fixtures.createSite("B", "1224"));
        expect(siteDao.getByAssignedIdentifiers(expectedIdentifiers)).andReturn(expectedSites);

        replayMocks();
        assertSame(expectedSites, mapping.getApplicationInstances(expectedIdentifiers));
        verifyMocks();
    }
}