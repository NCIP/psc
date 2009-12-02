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
import static java.util.Arrays.asList;
import java.util.Calendar;
import java.util.Collections;
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
    private Site uic;
    private Study nu123;

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

        nu = createSite("NU");
        uic = createSite("UIC");

        nu123 = new Study();
        nu123.setAssignedIdentifier("NU123");
        nu123.setProvider("alpha");
        nu123.setLastRefresh(INITIAL_REFRESH);

        expect(tools.getServices(StudySiteProvider.class)).andReturn(providers);
    }

    public void testNewStudySiteAddedOnRefresh() {
        associate(nu123, nu);

        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createBasicStudySite(null, nu),
            createBasicStudySite(null, uic)
        )));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Sites", 2, results.size());

        assertEquals("Wrong Site", "NU", results.get(0).getSite().getName());
        assertEquals("Wrong Site", "UIC", results.get(1).getSite().getName());
    }

    public void testNoDuplicateStudySitesOnRefresh() {
        associate(nu123, nu);

        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(asList(
            createBasicStudySite(null, nu)
        )));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Sites", 1, results.size());
        assertEquals("Wrong Site", "NU", results.get(0).getSite().getName());
    }

    public void testDeletedStudySiteExistsOnRefresh() {
        associate(nu123, nu);

        expect(providerA.getAssociatedSites(asList(nu123))).andReturn(asList(Collections.<StudySite>emptyList()));

        replayMocks();
        List<StudySite> results = consumer.refresh(nu123);
        verifyMocks();

        assertEquals("Wrong Number of Sites", 1, results.size());
        assertEquals("Wrong Site", "NU", results.get(0).getSite().getName());
    }



    ///// Helper Methods

    private StudySite associate(Study study, Site site) {
        StudySite ss = Fixtures.createStudySite(study, site);
        ss.setStudy(study);
        ss.setSite(site);
        return ss;
    }

    
            //        fromA.setProvider("alpha");
//        fromA.setLastRefresh(INITIAL_REFRESH);


    public static StudySite createBasicStudySite(Study study, Site site) {
        StudySite studySite = new StudySite();
        if (study != null) studySite.setStudy(study);
        if (site != null) studySite.setSite(site);
        return studySite;
    }

    ///// Stub interface
    private interface RefreshableStudySiteProvider extends StudySiteProvider, RefreshableProvider { }
}
