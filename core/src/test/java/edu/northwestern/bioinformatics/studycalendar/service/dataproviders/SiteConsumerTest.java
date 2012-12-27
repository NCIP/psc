/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.EasyMock.expect;
import org.easymock.IExpectationSetters;
import org.easymock.classextension.EasyMock;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Note: this class also tests the generic behaviors in AbstractConsumer.
 *
 * @author Rhett Sutphin
 */
public class SiteConsumerTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2009, Calendar.JANUARY, 6, 3, 4, 5);
    private static final Timestamp INITIAL_REFRESH = DateTools.createTimestamp(2007, Calendar.APRIL, 1);

    private SiteConsumer consumer;
    private OsgiLayerTools tools;
    private SiteProvider providerA;
    private RefreshableSiteProvider providerG;
    private List<SiteProvider> providers;
    private StaticNowFactory nowFactory;
    private Site fromA, searchA, fromG, searchG;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(NOW);
        tools = registerMockFor(OsgiLayerTools.class);

        consumer = new SiteConsumer();
        consumer.setOsgiLayerTools(tools);
        consumer.setNowFactory(nowFactory);

        providerA = registerMockFor(SiteProvider.class);
        providerG = registerMockFor(RefreshableSiteProvider.class);
        providers = Arrays.asList(providerA, providerG);

        expect(providerA.providerToken()).andStubReturn("alpha");
        expect(providerG.providerToken()).andStubReturn("gamma");
        expect(providerG.getRefreshInterval()).andStubReturn(20 * 60);

        fromA = Fixtures.createSite("From A", "A0");
        fromA.setProvider("alpha");
        fromA.setLastRefresh(INITIAL_REFRESH);
        fromG = Fixtures.createSite("From G", "G1");
        fromG.setProvider("gamma");
        fromG.setLastRefresh(INITIAL_REFRESH);

        searchA = Fixtures.createSite("Search A", "Ab");
        searchG = Fixtures.createSite("Search G", "Gh");
    }

    public void testGetSiteUsesTheSpecifiedProvider() throws Exception {
        Site expected = new Site();
        expectSiteRetrievedFromProviderG("foo", expected);
        replayMocks();

        assertSame(expected, consumer.getSite("foo", "gamma"));
        verifyMocks();
    }

    private void expectSiteRetrievedFromProviderG(String ident, Site expected) {
        expectAllProvidersAvailable();
        expectGetOneSite(providerG, ident).andReturn(Arrays.asList(expected));
    }

    private void expectAllProvidersAvailable() {
        expect(tools.getServices(SiteProvider.class)).andReturn(providers);
    }

    private IExpectationSetters<List<Site>> expectGetOneSite(SiteProvider provider, String ident) {
        return expect(provider.getSites(Arrays.asList(ident)));
    }

    public void testGetSiteReturnsNullIfNoServices() throws Exception {
        expect(tools.getServices(SiteProvider.class)).andReturn(Collections.<SiteProvider>emptyList());
        replayMocks();

        assertNull(consumer.getSite("foo", "gamma"));
        verifyMocks();
    }

    public void testGetSiteReturnsNullIfNoMatchingService() throws Exception {
        expect(tools.getServices(SiteProvider.class)).andReturn(Arrays.asList(providerA));
        replayMocks();

        assertNull(consumer.getSite("foo", "gamma"));
        verifyMocks();
    }
    
    public void testGetSiteReturnsNullIfProviderReturnsNull() throws Exception {
        expectSiteRetrievedFromProviderG("foo", null);
        replayMocks();

        assertNull(consumer.getSite("foo", "gamma"));
        verifyMocks();
    }

    public void testGetSiteSetsTheProviderTokenOnTheNewSite() throws Exception {
        expectSiteRetrievedFromProviderG("foo", new Site());
        replayMocks();

        Site actual = consumer.getSite("foo", "gamma");
        verifyMocks();

        assertEquals("Provider not set", "gamma", actual.getProvider());
    }

    public void testGetSiteSetsTheRefreshTimeOnTheNewSite() throws Exception {
        expectSiteRetrievedFromProviderG("foo", new Site());
        replayMocks();

        Site actual = consumer.getSite("foo", "gamma");
        verifyMocks();

        assertEquals("Refresh not set", NOW, actual.getLastRefresh());
    }

    public void testSearchAggregatesResultsFromMultipleProviders() throws Exception {
        String q = "from";
        expectSuccessfulSearch(q);
        replayMocks();

        List<Site> actual = consumer.search(q);
        assertEquals("Wrong number of results: " + actual, 2, actual.size());
        assertSame("Missing provider A site", searchA, actual.get(0));
        assertSame("Missing provider G site", searchG, actual.get(1));
        verifyMocks();
    }

    public void testSearchReturnsNoResultsIfNoServices() throws Exception {
        expect(tools.getServices(SiteProvider.class)).andReturn(Collections.<SiteProvider>emptyList());
        replayMocks();

        assertTrue(consumer.search("foo").isEmpty());
        verifyMocks();
    }

    public void testSearchSetsProviderOnFoundSites() throws Exception {
        expectSuccessfulSearch("from");
        replayMocks();

        List<Site> actual = consumer.search("from");
        assertEquals("Site from provider A has wrong token", "alpha", actual.get(0).getProvider());
        assertEquals("Site from provider G has wrong token", "gamma", actual.get(1).getProvider());
    }

    public void testSearchSetsRefreshTimeOnFoundSites() throws Exception {
        expectSuccessfulSearch("from");
        replayMocks();

        List<Site> actual = consumer.search("from");
        assertEquals("Site from provider A has wrong time", NOW, actual.get(0).getLastRefresh());
        assertEquals("Site from provider G has wrong time", NOW, actual.get(1).getLastRefresh());
    }

    public void testSearchWorksIfAProviderReturnsNull() throws Exception {
        String q = "no";
        expectAllProvidersAvailable();
        expect(providerA.search(q)).andReturn(null);
        expect(providerG.search(q)).andReturn(Arrays.asList(searchG));
        replayMocks();

        List<Site> actual = consumer.search(q);
        assertEquals("Wrong number of results", 1, actual.size());
        assertEquals("Wrong result", searchG, actual.get(0));
    }

    private void expectSuccessfulSearch(String q) {
        expectAllProvidersAvailable();
        expect(providerA.search(q)).andReturn(Arrays.asList(searchA));
        expect(providerG.search(q)).andReturn(Arrays.asList(searchG));
    }

    public void testRefreshUpdatesTheExistingSiteInPlace() throws Exception {
        Site newVersion = Fixtures.createSite("From G v2", "G1");
        expectSiteRetrievedFromProviderG("G1", newVersion);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals("From G v2", fromG.getName());
        verifyMocks();
    }
    
    public void testSuccessfulRefreshUpdatesTheRefreshTime() throws Exception {
        Site newVersion = Fixtures.createSite("From G v2", "G1");
        expectSiteRetrievedFromProviderG("G1", newVersion);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals(NOW, fromG.getLastRefresh());
        verifyMocks();
    }

    public void testRefreshReturnsItsInputForChaining() throws Exception {
        expectAllProvidersAvailable();
        replayMocks();

        assertEquals(Arrays.asList(searchG), consumer.refresh(Arrays.asList(searchG)));
        verifyMocks();
    }
    
    public void testRefreshOneJustDelegates() throws Exception {
        SiteConsumer partialMock = EasyMock.createMock(
            SiteConsumer.class, new Method[] { SiteConsumer.class.getMethod("refresh", List.class) });
        expect(partialMock.refresh(Arrays.asList(fromG))).andReturn(Arrays.asList(fromG));
        EasyMock.replay(partialMock);

        assertSame(fromG, partialMock.refresh(fromG));
        EasyMock.verify(partialMock);
    }

    public void testRefreshHappensWithNullLastRefresh() throws Exception {
        fromG.setLastRefresh(null);
        Site newVersion = Fixtures.createSite("From G v2", "G1");
        expectSiteRetrievedFromProviderG("G1", newVersion);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals(NOW, fromG.getLastRefresh());
        verifyMocks();
    }
    
    public void testRefreshDoesNothingWithoutAMatchingProvider() throws Exception {
        expect(tools.getServices(SiteProvider.class)).andReturn(Arrays.asList(providerA));
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals(INITIAL_REFRESH, fromG.getLastRefresh());
        verifyMocks();
    }

    public void testRefreshDoesNothingIfThereIsAProviderError() throws Exception {
        expectAllProvidersAvailable();
        expectGetOneSite(providerG, "G1").andThrow(new IllegalStateException("Time to panic"));
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals(INITIAL_REFRESH, fromG.getLastRefresh());
        verifyMocks();
    }

    public void testRefreshDoesNothingForNonProvidedSites() throws Exception {
        Site local = Fixtures.createSite("L");
        expectAllProvidersAvailable();
        replayMocks();

        consumer.refresh(Arrays.asList(local));
        verifyMocks();
    }

    public void testRefreshDoesNothingForNonRefreshableProviders() throws Exception {
        expectAllProvidersAvailable();
        replayMocks();

        consumer.refresh(Arrays.asList(fromA));
        verifyMocks();
    }

    public void testRefreshDoesNothingForRefreshIntervalNull() throws Exception {
        expectAllProvidersAvailable();
        expect(providerG.getRefreshInterval()).andReturn(null);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        verifyMocks();
    }

    public void testRefreshDoesNothingForRefreshIntervalNegative() throws Exception {
        expectAllProvidersAvailable();
        expect(providerG.getRefreshInterval()).andReturn(-18);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        verifyMocks();
    }

    public void testRefreshDoesNothingForRequestWithinRefreshInterval() throws Exception {
        Timestamp recent = new Timestamp(NOW.getTime() - 30 * 1000);
        fromG.setLastRefresh(recent);
        expectAllProvidersAvailable();
        expect(providerG.getRefreshInterval()).andReturn(60);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        verifyMocks();
    }

    public void testRefreshListOnlyInvokesEachProviderOnce() throws Exception {
        Site fromG1 = createSite("From G1", "G,"); fromG1.setProvider("gamma");
        Site fromG2 = createSite("From G2", "G."); fromG2.setProvider("gamma");

        expectAllProvidersAvailable();
        expect(providerG.getSites(Arrays.asList("G1", "G,", "G."))).andReturn(
            Arrays.asList(createSite("From G"), createSite("From G1 upd"), createSite("From G2")));
        replayMocks();

        consumer.refresh(Arrays.asList(fromG, fromG1, fromG2));
        verifyMocks();
    }

    public void testRefreshListCorrelatesInlineUpdates() throws Exception {
        Site fromG1 = createSite("From G1", "G,"); fromG1.setProvider("gamma");
        Site fromG2 = createSite("From G2", "G."); fromG2.setProvider("gamma");

        expectAllProvidersAvailable();
        expect(providerG.getSites(Arrays.asList("G1", "G,", "G."))).andReturn(
            Arrays.asList(createSite("From G"), createSite("From G1 upd"), createSite("From G2")));
        replayMocks();

        consumer.refresh(Arrays.asList(fromG, fromG1, fromG2));
        assertEquals("From G1 upd", fromG1.getName());
        verifyMocks();
    }

    public void testRefreshListDoesNotFailWithGoneMissingSites() throws Exception {
        Site fromG1 = createSite("From G1", "G,"); fromG1.setProvider("gamma");
        Site fromG2 = createSite("From G2", "G."); fromG2.setProvider("gamma");

        expectAllProvidersAvailable();
        expect(providerG.getSites(Arrays.asList("G1", "G,", "G."))).andReturn(
            Arrays.<Site>asList(null, null, null));
        replayMocks();

        consumer.refresh(Arrays.asList(fromG, fromG1, fromG2));
        verifyMocks();
    }

    public void testRefreshDoesNothingForProviderThatIncorrectlyReturnsNull() throws Exception {
        expectAllProvidersAvailable();
        expect(providerG.getSites(Arrays.asList("G1"))).andReturn(null);
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals("Should not be updated", INITIAL_REFRESH, fromG.getLastRefresh());
        verifyMocks();
    }

    public void testRefreshDoesNothingForProviderThatIncorrectlyReturnsTheWrongNumberOfResults()
        throws Exception
    {
        expectAllProvidersAvailable();
        expect(providerG.getSites(Arrays.asList("G1"))).
            andReturn(Arrays.asList(new Site(), new Site()));
        replayMocks();

        consumer.refresh(Arrays.asList(fromG));
        assertEquals("Should not be updated", INITIAL_REFRESH, fromG.getLastRefresh());
        verifyMocks();
    }

    private interface RefreshableSiteProvider extends SiteProvider, RefreshableProvider { }
}
