package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.studyprotocolservice.client.StudyProtocolServiceClient;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import org.iso._21090.II;
import org.iso._21090.ST;

import java.util.List;

/**
 * @author John Dzak
 */
public class CoppaStudyProviderTest extends TestCase{
    private CoppaStudyProvider provider;
    private StudyProtocolServiceClient client;
    private MockRegistry mocks = new MockRegistry();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = mocks.registerMockFor(StudyProtocolServiceClient.class);

        provider = new CoppaStudyProvider();
        provider.setClient(client);
    }

    public void testSearchWithOneResult() throws Exception{
        expect(client.search((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(new StudyProtocol[]{
                coppaStudyProtocol("NCI-123", "NCI", "NCI Tissue Banking Protocol")
        });
        mocks.replayMocks();

        List<Study> actual = provider.search("NCI");

        assertEquals("Incorrect number of studies returned", 1, actual.size());
        assertStudy("Wrong study created", "NCI-123", "NCI Tissue Banking Protocol", actual.get(0));
    }

    public void testSearchWithNoResults() throws Exception{
        expect(client.search((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NOTHING");

        assertTrue("Incorrect number of studies returned", actual.isEmpty());
    }

    public void testCreateStudy() throws Exception {
        StudyProtocol sp = coppaStudyProtocol("NCI-123", "NCI", "NCI Protocol Official Name", "NCI Protocol Public Name");

        Study actual = provider.createStudy(sp);

        assertEquals("Wrong extension", "NCI-123", actual.getSecondaryIdentifierValue("extension"));
        assertEquals("Wrong root", "NCI", actual.getSecondaryIdentifierValue("root"));
        assertEquals("Wrong public title", "NCI Protocol Public Name", actual.getSecondaryIdentifierValue("publicTitle"));
        assertEquals("Wrong official title", "NCI Protocol Official Name", actual.getSecondaryIdentifierValue("officialTitle"));
    }
    
    // Helper Methods
    private void assertStudy(String msg, String expectedAssignedId, String expectedLongTitle, Study actual) {
        assertEquals(msg + ": wrong name",  expectedAssignedId,  actual.getAssignedIdentifier());
        assertEquals(msg + ": wrong ident", expectedLongTitle, actual.getLongTitle());
    }

    private StudyProtocol coppaStudyProtocol(String extension, String root, String officalTitle) {
        StudyProtocol p = new StudyProtocol();

        II ii = new II();
        ii.setExtension(extension);
        ii.setRoot(root);
        p.setAssignedIdentifier(ii);

        ST st = new ST();
        st.setValue(officalTitle);
        p.setOfficialTitle(st);

        return p;
    }

    private StudyProtocol coppaStudyProtocol(String extension, String root, String officialTitle, String publicTitle) {
        StudyProtocol p = coppaStudyProtocol(extension, root, officialTitle);

        ST pub = new ST();
        pub.setValue(publicTitle);
        p.setPublicTitle(pub);

        return p;
    }
}
