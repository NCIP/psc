package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.EasyMock.expect;

import java.sql.Timestamp;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class StudySiteConsumerTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2009, Calendar.JANUARY, 6, 3, 4, 5);
    private static final Timestamp NOW_MINUS_A_SECOND = DateTools.createTimestamp(2009, Calendar.JANUARY, 6, 3, 4, 4);
    private static final Timestamp INITIAL_REFRESH = DateTools.createTimestamp(2007, Calendar.APRIL, 1);

    private OsgiLayerTools tools;
    private StudySiteConsumer consumer;
    private RefreshableStudySiteProvider providerA;
    private StaticNowFactory nowFactory;
    private List<StudySiteProvider> providers;

    private Site nu;
    private Site uic;
    private Study nu123;

    private Site nuSkel;
    private Site uicSkel;

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

        nu = createSite("NU", "NU");
        uic = createSite("UIC", "UIC");

        nuSkel = createSiteSkel("NU");
        uicSkel = createSiteSkel("UIC");

        nu123 = new Study();
        nu123.setAssignedIdentifier("NU123");
        nu123.setProvider("alpha");
        nu123.setLastRefresh(INITIAL_REFRESH);

//        ;
    }

    private Site createSiteSkel(String assignedIdentifier) {
        Site s = new Site();
        s.setAssignedIdentifier(assignedIdentifier);
        return s;
    }

    private void expectProviders(List<StudySiteProvider> providers) {
        expect(tools.getServices(StudySiteProvider.class)).andReturn(providers).anyTimes();
    }

    public void testNewStudySiteAddedOnRefresh() {
        associate(nu123, nu);

        expectProviders(providers);
        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createBasicStudySite(null, nuSkel),
            createBasicStudySite(null, uicSkel)
        )));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Sites", 2, results.size());

        assertEquals("Wrong Site", "NU", results.get(0).getSite().getAssignedIdentifier());
        assertEquals("Wrong Site", "UIC", results.get(1).getSite().getAssignedIdentifier());

        assertEquals("Wrong provider", "alpha", results.get(0).getProvider());
        assertEquals("Wrong provider", "alpha", results.get(1).getProvider());
    }

    public void testNoDuplicateStudySitesOnRefresh() {
        associate(nu123, nu);

        expectProviders(providers);
        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createBasicStudySite(null, nuSkel)
        )));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Study Sites", 1, results.size());
        assertEquals("Wrong Site", "NU", results.get(0).getSite().getAssignedIdentifier());
    }

    public void testDeletedStudySiteExistsOnRefresh() {
        associate(nu123, nu);

        expectProviders(providers);
        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(Collections.<StudySite>emptyList()));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Study Sites", 1, results.size());
        assertEquals("Wrong Site", "NU", results.get(0).getSite().getAssignedIdentifier());
    }

    public void testLastRefreshTimestampWhenRefreshed() {
        associate(nu123, nu);

        expectProviders(providers);
        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createBasicStudySite(null, nuSkel)
        )));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Last Refresh Time", NOW, results.get(0).getLastRefresh());
    }

    public void testLastRefreshTimestampIsNotUpdated() {
        StudySite s = associate(nu123, nu);
        s.setLastRefresh(NOW_MINUS_A_SECOND);

        expectProviders(providers);

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Last Refresh Time", NOW_MINUS_A_SECOND , results.get(0).getLastRefresh());
    }

    public void testRefreshHappensWhenNoStudySitesExist() {
        expectProviders(providers);

        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createBasicStudySite(null, nuSkel)
        )));
        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Study Sites", 1, results.size());
        assertEquals("Wrong Site", "NU", results.get(0).getSite().getAssignedIdentifier());
    }

    public void testRefreshWithStudyNotFromProvider() {
        Study notFromProvider = createStudySkel("Not From Provider");

        expect(providerA.getAssociatedSites(asList(notFromProvider, nu123))).andReturn(asList(
                new ArrayList<StudySite>(),
                asList(createBasicStudySite(null, nuSkel))
        ));

        StudySiteProvider providerB = registerMockFor(NonRefreshableStudySiteProvider.class);
        expect(providerB.providerToken()).andReturn("Non Refreshable Provider").anyTimes();
        expectProviders(asList(providerA, providerB));

        replayMocks();
        List<List<StudySite>> results = consumer.refresh(asList(notFromProvider, nu123));
        verifyMocks();

        assertEquals("Wrong Number of Study Sites Lists", 2, results.size());

        List<StudySite> ss0 = results.get(0);
        assertEquals("Wrong Number of Study Sites", 0, ss0.size());

        List<StudySite> ss1 = results.get(1);
        assertEquals("Wrong Number of Study Sites", 1, ss1.size());
        assertEquals("Wrong Site", "NU", ss1.get(0).getSite().getAssignedIdentifier());
    }

    //// Site Based Refresh
    public void testSiteBasedNewStudySiteAddedOnRefresh() {
        associate(nu123, nu);

        Study nu123Skel = createStudySkel("NU123");
        Study nci999Skel = createStudySkel("NCI999");

        expectProviders(providers);
        expect(providerA.getAssociatedStudies(asList(nu))).andReturn(asList(asList(
            createBasicStudySite(nu123Skel, null),
            createBasicStudySite(nci999Skel, null)
        )));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu);
        verifyMocks();

        assertEquals("Wrong Number of Sites", 2, results.size());

        assertEquals("Wrong Site", "NU123", results.get(0).getStudy().getAssignedIdentifier());
        assertEquals("Wrong Site", "NCI999", results.get(1).getStudy().getAssignedIdentifier());
    }

    private Study createStudySkel(String assignedIdentifier) {
        Study s = new Study();
        s.setAssignedIdentifier(assignedIdentifier);
        return s;
    }

    ///// Helper Methods

    private StudySite associate(Study study, Site site) {
        StudySite ss = Fixtures.createStudySite(study, site);
        ss.setStudy(study);
        ss.setSite(site);
        ss.setLastRefresh(INITIAL_REFRESH);
        ss.setProvider("alpha");
        return ss;
    }

    public static StudySite createBasicStudySite(Study study, Site site) {
        StudySite studySite = new StudySite();
        if (study != null) studySite.setStudy(study);
        if (site != null) studySite.setSite(site);
        return studySite;
    }

    ///// Stub interface
    private interface RefreshableStudySiteProvider extends StudySiteProvider, RefreshableProvider { }
    private interface NonRefreshableStudySiteProvider extends StudySiteProvider { }
}
