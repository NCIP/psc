package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import gov.nih.nci.coppa.services.pa.studyprotocolservice.client.StudyProtocolServiceClient;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import org.iso._21090.CD;
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
    private StudySiteServiceClient studySiteClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = mocks.registerMockFor(StudyProtocolServiceClient.class);
        studySiteClient = mocks.registerMockFor(StudySiteServiceClient.class);

        provider = new CoppaStudyProvider();
        provider.setClient(client);
        provider.setStudySiteClient(studySiteClient);
    }

    public void testSearchWithOneResult() throws Exception{
        expect(client.search((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(new StudyProtocol[]{
            coppaStudyProtocol("NCI-123", "NCI Tissue Banking Protocol", "NCI Public Title")
        });
        expect(studySiteClient.getByStudyProtocol((Id) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NCI");

        assertEquals("Incorrect number of studies returned", 1, actual.size());
        assertStudy("Wrong study created", "NCI-123", "NCI Tissue Banking Protocol", actual.get(0));
    }

    private StudySite coppaLeadStudySite(String localIdentifier) {
        StudySite ss = new StudySite();

        ST st = new ST();
        st.setValue(localIdentifier);
        ss.setLocalStudyProtocolIdentifier(st);

        CD cd = new CD();
        cd.setCode("Lead Organization");
        ss.setFunctionalCode(cd);

        return ss;
    }

    public void testSearchWithNoResults() throws Exception{
        expect(client.search((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NOTHING");

        assertTrue("Incorrect number of studies returned", actual.isEmpty());
    }

    public void testCreateStudy() throws Exception {
        expect(studySiteClient.getByStudyProtocol((Id) notNull())).andReturn(new StudySite[] {
            coppaLeadStudySite("My Local Identifier")
        });
        mocks.replayMocks();

        StudyProtocol sp = coppaStudyProtocol("NCI-123", "NCI Protocol Official Title", "NCI Protocol Public Title");
        Study actual = provider.createStudy(sp);

        assertEquals("Wrong extension", "NCI-123", actual.getSecondaryIdentifierValue("extension"));
        assertEquals("Wrong public title", "NCI Protocol Public Title", actual.getSecondaryIdentifierValue("publicTitle"));
        assertEquals("Wrong official title", "NCI Protocol Official Title", actual.getSecondaryIdentifierValue("officialTitle"));
        assertEquals("Wrong local study protocol identifier", "My Local Identifier", actual.getSecondaryIdentifierValue("localStudyProtocolIdentifier"));
    }
    
    // Helper Methods
    private void assertStudy(String msg, String expectedAssignedId, String expectedLongTitle, Study actual) {
        assertEquals(msg + ": wrong name",  expectedAssignedId,  actual.getAssignedIdentifier());
        assertEquals(msg + ": wrong ident", expectedLongTitle, actual.getLongTitle());
    }

    private StudyProtocol coppaStudyProtocol(String extension, String officialTitle, String publicTitle) {
        StudyProtocol p = new StudyProtocol();

        II ii = new II();
        ii.setExtension(extension);
        p.setAssignedIdentifier(ii);

        ST st = new ST();
        st.setValue(officialTitle);
        p.setOfficialTitle(st);

        ST pub = new ST();
        pub.setValue(publicTitle);
        p.setPublicTitle(pub);

        return p;
    }
}
