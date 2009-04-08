package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class SiteProviderAdapterTest extends StudyCalendarTestCase {
    private SiteProviderAdapter adapter;
    private OsgiLayerTools tools;
    private SiteProvider mockSiteProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tools = registerMockFor(OsgiLayerTools.class);
        adapter = new SiteProviderAdapter();
        adapter.setOsgiLayerTools(tools);

        mockSiteProvider = registerMockFor(SiteProvider.class);
    }

    public void testGetSiteDelegatesToFirstAvailableService() throws Exception {
        Site expected = new Site();
        expect(tools.getOptionalService(SiteProvider.class.getName())).andReturn(mockSiteProvider);
        expect(mockSiteProvider.getSite("foo")).andReturn(expected);
        replayMocks();

        assertSame(expected, adapter.getSite("foo"));
        verifyMocks();
    }

    public void testGetSiteReturnsNullIfNoService() throws Exception {
        expect(tools.getOptionalService(SiteProvider.class.getName())).andReturn(null);
        replayMocks();

        assertNull(adapter.getSite("foo"));
        verifyMocks();
    }

    public void testSearchDelegatesToFirstAvailableService() throws Exception {
        List<Site> expected = Arrays.asList(new Site());
        expect(tools.getOptionalService(SiteProvider.class.getName())).andReturn(mockSiteProvider);
        expect(mockSiteProvider.search("foo")).andReturn(expected);
        replayMocks();

        assertSame(expected, adapter.search("foo"));
        verifyMocks();
    }

    public void testSearchReturnsNoResultsIfNoService() throws Exception {
        expect(tools.getOptionalService(SiteProvider.class.getName())).andReturn(null);
        replayMocks();

        assertTrue(adapter.search("foo").isEmpty());
        verifyMocks();
    }
}
