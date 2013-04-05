/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class SitesInitializerTest extends SchemaInitializerTestCase {
    private SiteService siteService;
    private SiteDao siteDao;

    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        siteDao = registerMockFor(SiteDao.class);
    }

    @SuppressWarnings({ "ConstantConditions" })
    public void testIsRowPreservingInitializerForUserRoleSitesTable() throws Exception {
        SitesInitializer initializer = createInitializer("");
        assertTrue(initializer instanceof RowPreservingInitializer);
        assertEquals("Wrong table", "sites", initializer.getTableName());
        assertEquals("Wrong PKs", Arrays.asList("id"), initializer.getPrimaryKeyNames());
    }
    
    public void testCreatesNewSiteIfDoesNotExist() throws Exception {
        expect(siteDao.getByAssignedIdentifier("CA001")).andReturn(null);
        Site site = Fixtures.createSite("CoH", "CA001");
        expect(siteService.createOrUpdateSite(site)).andReturn(site);

        replayMocks();
        createInitializer("coh:\n  name: CoH\n  assignedIdentifier: CA001\n").oneTimeSetup(connectionSource);
        verifyMocks();
    }
    
    public void testUpdatesSiteIfExists() throws Exception {
        Site existingSite = Fixtures.createSite("CoH", "CA001");
        expect(siteDao.getByAssignedIdentifier("CA001")).andReturn(existingSite);
        Site updatedSite = Fixtures.createSite("Hope", "CA001");
        expect(siteService.createOrUpdateSite(updatedSite)).andReturn(updatedSite);

        replayMocks();
        createInitializer("coh:\n  name: Hope\n  assignedIdentifier: CA001\n").oneTimeSetup(connectionSource);
        verifyMocks();
    }

    private SitesInitializer createInitializer(String yaml) throws Exception {
        SitesInitializer initializer = new SitesInitializer();
        initializer.setSiteService(siteService);
        initializer.setSiteDao(siteDao);
        initializer.setYamlResource(literalYamlResource(yaml));
        initializer.afterPropertiesSet();
        return initializer;
    }
}
