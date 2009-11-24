package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.EasyMock.expect;

import java.sql.Timestamp;
import static java.util.Arrays.asList;
import java.util.Calendar;
import java.util.List;

public class StudySiteConsumerTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2009, Calendar.JANUARY, 6, 3, 4, 5);
    private static final Timestamp INITIAL_REFRESH = DateTools.createTimestamp(2007, Calendar.APRIL, 1);

    private OsgiLayerTools tools;
    private StudySiteConsumer consumer;
    private RefreshableStudySiteProvider providerA;
    private StaticNowFactory nowFactory;
    private List<StudySiteProvider> providers;

    private Site nu;
    private Study nu123;
    private StudySite fromA;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(NOW);
        tools = registerMockFor(OsgiLayerTools.class);

        consumer = new StudySiteConsumer();
        consumer.setOsgiLayerTools(tools);
        consumer.setNowFactory(nowFactory);

        providerA = registerMockFor(RefreshableStudySiteProvider.class);
        providers = asList((StudySiteProvider) providerA);

        expect(providerA.providerToken()).andStubReturn("alpha");
        expect(providerA.getRefreshInterval()).andStubReturn(20 * 60);

        nu123 = new Study();
        nu123.setAssignedIdentifier("NU123");
        nu = createSite("Northwestern");
        fromA = createStudySite(nu123, nu);
        fromA.setProvider("alpha");
        fromA.setLastRefresh(INITIAL_REFRESH);

        expect(tools.getServices(StudySiteProvider.class)).andReturn(providers);
    }

    public void testStudySiteExistsOnRefresh() {
        Site uic = createSite("UIC");

        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createStudySite(nu123, uic),
            createStudySite(nu123, nu)
        )));

        replayMocks();
        StudySite result = consumer.refresh(fromA);
        verifyMocks();

        assertNotNull("StudySite should still exist", result);
        assertEquals("Wrong Study", "NU123", result.getStudy().getAssignedIdentifier());
        assertEquals("Wrong Study", "Northwestern", result.getSite().getName());
    }

//    public void testStudySitePrunedOnRefresh() {
//        assertTrue(false);
//    }

    private interface RefreshableStudySiteProvider extends StudySiteProvider, RefreshableProvider { }
}
