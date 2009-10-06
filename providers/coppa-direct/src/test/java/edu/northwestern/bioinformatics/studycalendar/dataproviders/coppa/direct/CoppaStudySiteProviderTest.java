package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Dzak
 */
public class CoppaStudySiteProviderTest extends TestCase {
    private CoppaStudySiteProvider provider;
    private StudySiteServiceClient client;
    private MockRegistry mocks = new MockRegistry();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = mocks.registerMockFor(StudySiteServiceClient.class);

        provider = new CoppaStudySiteProvider();
        provider.setClient(client);
    }

    public void testGetAssociatedSitesWithEmptyStudyList() throws Exception {
        expect(client.getByStudyProtocol((Id) EasyMock.notNull())).andReturn(
                new gov.nih.nci.coppa.services.pa.StudySite[0]
        );

        List<List<StudySite>> actual = provider.getAssociatedSites(new ArrayList<Study>());

        assertEquals("Wrong results size", 0, actual.size());
    }
}
