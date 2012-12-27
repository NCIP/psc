/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.providers;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.mock.MockDataProviderTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions;

import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;

/**
 * @author Rhett Sutphin
 */
public class SiteConsumerIntegratedTest extends OsgiLayerIntegratedTestCase {
    private static final String MOCK_PROVIDERS_SYMBOLIC_NAME = "edu.northwestern.bioinformatics.psc-providers-mock";
    private Site site;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        site = Fixtures.createSite("Iowa Lutheran Hospital", "IA030");
        site.setProvider(MockDataProviderTools.PROVIDER_TOKEN);
        getSiteService().createOrUpdateSite(site);
        startBundle(MOCK_PROVIDERS_SYMBOLIC_NAME, SiteProvider.class.getName());

        dumpBundles();
    }

    @Override
    public void tearDown() throws Exception {
        stopBundle(MOCK_PROVIDERS_SYMBOLIC_NAME);

        getSiteService().removeSite(site);
        super.tearDown();
    }

    public void testRefreshDoesNotFail() throws Exception {
        Site refreshed = getSiteConsumer().refresh(site);
        assertEquals("Iowa Lutheran Hospital", refreshed.getName());
        assertNotNull("Could not find test site", refreshed);
        assertNotNull("Test site not refreshed", refreshed.getLastRefresh());
        MoreJUnitAssertions.assertDatesClose("Not refreshed recently", new Date(), refreshed.getLastRefresh(), 25000);
    }

    ////// HELPERS

    private SiteService getSiteService() {
        return ((SiteService) getApplicationContext().getBean("siteService"));
    }

    private SiteConsumer getSiteConsumer() {
        return (SiteConsumer) getApplicationContext().getBean("siteConsumer");
    }
}
