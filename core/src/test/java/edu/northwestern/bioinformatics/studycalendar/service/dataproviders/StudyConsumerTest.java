/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addSecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.EasyMock.expect;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyConsumerTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2009, Calendar.JANUARY, 6, 3, 4, 5);
    private static final Timestamp INITIAL_REFRESH = DateTools.createTimestamp(2007, Calendar.APRIL, 1);

    private StudyConsumer consumer;
    private OsgiLayerTools tools;
    private StudyProvider providerA;
    private RefreshableStudyProvider providerG;
    private StaticNowFactory nowFactory;
    private Study fromA, searchA, fromG, searchG;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        searchA = createNamedInstance("Search A", Study.class);
        searchG = createNamedInstance("Search G", Study.class);

        tools = registerMockFor(OsgiLayerTools.class);
        providerA = registerMockFor(StudyProvider.class);
        providerG = registerMockFor(RefreshableStudyProvider.class);
        nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(NOW);

        consumer = new StudyConsumer();
        consumer.setOsgiLayerTools(tools);
        consumer.setNowFactory(nowFactory);

        expect(providerA.providerToken()).andStubReturn("alpha");
        expect(providerG.providerToken()).andStubReturn("gamma");
        expect(providerG.getRefreshInterval()).andStubReturn(20 * 60);

        fromA = createNamedInstance("From A", Study.class);
        fromA.setProvider("alpha");
        fromA.setLastRefresh(INITIAL_REFRESH);
        fromG = createNamedInstance("From G", Study.class);
        fromG.setProvider("gamma");
        fromG.setLastRefresh(INITIAL_REFRESH);
    }
    
    public void testSearchAggregatesResultsFromMultipleProviders() throws Exception {
        String q = "from";
        expectSuccessfulSearch(q);
        replayMocks();

        List<Study> actual = consumer.search(q);
        assertEquals("Wrong number of results: " + actual, 2, actual.size());
        assertSame("Missing provider A study", searchA, actual.get(0));
        assertSame("Missing provider G study", searchG, actual.get(1));
        verifyMocks();
    }

    public void testSearchReturnsNoResultsIfNoServices() throws Exception {
        expectProvidersAvailable();
        replayMocks();

        assertTrue(consumer.search("foo").isEmpty());
        verifyMocks();
    }

    public void testSearchSetsProviderOnFoundStudies() throws Exception {
        expectSuccessfulSearch("from");
        replayMocks();

        List<Study> actual = consumer.search("from");
        assertEquals("Study from provider A has wrong token", "alpha", actual.get(0).getProvider());
        assertEquals("Study from provider G has wrong token", "gamma", actual.get(1).getProvider());
    }

    public void testSearchSetsRefreshTimeOnFoundStudies() throws Exception {
        expectSuccessfulSearch("from");
        replayMocks();

        List<Study> actual = consumer.search("from");
        assertEquals("Study from provider A has wrong time", NOW, actual.get(0).getLastRefresh());
        assertEquals("Study from provider G has wrong time", NOW, actual.get(1).getLastRefresh());
    }

    public void testSearchWorksIfAProviderReturnsNull() throws Exception {
        String q = "no";
        expectProvidersAvailable(providerA, providerG);
        expect(providerA.search(q)).andReturn(null);
        expect(providerG.search(q)).andReturn(Arrays.asList(searchG));
        replayMocks();

        List<Study> actual = consumer.search(q);
        assertEquals("Wrong number of results", 1, actual.size());
        assertEquals("Wrong result", searchG, actual.get(0));
    }

    private void expectSuccessfulSearch(String q) {
        expectProvidersAvailable(providerA, providerG);
        expect(providerA.search(q)).andReturn(Arrays.asList(searchA));
        expect(providerG.search(q)).andReturn(Arrays.asList(searchG));
    }

    private void expectProvidersAvailable(StudyProvider... providers) {
        expect(tools.getServices(StudyProvider.class)).andReturn(Arrays.asList(providers));
    }

    public void testRefreshLeavesAssignedIdentifierAlone() throws Exception {
        Study newVersion = createNamedInstance("Else", Study.class);

        expectRefreshFromG(newVersion);
        assertEquals("Assigned identifier changed", "From G", fromG.getAssignedIdentifier());
    }

    public void testRefreshAddsNewSecondaryIdentifier() throws Exception {
        Study newVersion = fromG.clone();
        addSecondaryIdentifier(newVersion, "aleph", "naught");
        expectRefreshFromG(newVersion);
        assertEquals("Wrong number of identifiers", 1, fromG.getSecondaryIdentifiers().size());
        assertSecondaryIdentifier("Wrong new ident", "aleph", "naught",
            fromG.getSecondaryIdentifiers().first());
    }

    public void testRefreshLeavesUnchangedSecondaryIdentifier() throws Exception {
        addSecondaryIdentifier(fromG, "aleph", "zero");
        StudySecondaryIdentifier original = fromG.getSecondaryIdentifiers().first();

        expectRefreshFromG(fromG.clone());
        assertEquals("Wrong number of identifiers", 1, fromG.getSecondaryIdentifiers().size());
        assertSecondaryIdentifier("Old ident not preserved", "aleph", "zero",
            fromG.getSecondaryIdentifiers().first());
        assertSame("Old ident replaced", original, fromG.getSecondaryIdentifiers().first());
    }

    public void testRefreshRemovesUnreferencedSecondaryIdentifier() throws Exception {
        addSecondaryIdentifier(fromG, "aleph", "naught");
        addSecondaryIdentifier(fromG, "aleph", "null");

        Study newVersion = fromG.clone();
        newVersion.getSecondaryIdentifiers().remove(newVersion.getSecondaryIdentifiers().first());
        expectRefreshFromG(newVersion);

        assertEquals("Wrong number of remaining identifiers", 1, fromG.getSecondaryIdentifiers().size());
        assertSecondaryIdentifier("Wrong ident preserved", "aleph", "null",
            fromG.getSecondaryIdentifiers().first());
    }

    public void testRefreshUpdatesLongTitle() throws Exception {
        fromG.setLongTitle("Something");

        Study newVersion = fromG.clone();
        newVersion.setLongTitle("Else");
        expectRefreshFromG(newVersion);

        assertEquals("Wrong new long title", "Else", fromG.getLongTitle());
    }

    public void testRefreshUpdatesRefreshTime() throws Exception {
        expectRefreshFromG(fromG.clone());
        assertEquals("Refresh time not updated", NOW, fromG.getLastRefresh());
    }
    
    public void testRefreshReturnsTheInputStudy() throws Exception {
        expectProvidersAvailable();
        replayMocks();
        Study out = consumer.refresh(fromG);
        verifyMocks();

        assertSame(fromG, out);
    }

    private void expectRefreshFromG(Study newVersion) {
        expectProvidersAvailable(providerG);
        expect(providerG.getStudies(Arrays.asList(fromG))).andReturn(Arrays.asList(newVersion));

        replayMocks();
        consumer.refresh(fromG);
        verifyMocks();
    }

    private static void assertSecondaryIdentifier(
        String message, String expectedType, String expectedValue, StudySecondaryIdentifier actual
    ) {
        assertEquals(message + ": wrong type",  expectedType,  actual.getType());
        assertEquals(message + ": wrong value", expectedValue, actual.getValue());
    }

    private interface RefreshableStudyProvider extends StudyProvider, RefreshableProvider { }
}
