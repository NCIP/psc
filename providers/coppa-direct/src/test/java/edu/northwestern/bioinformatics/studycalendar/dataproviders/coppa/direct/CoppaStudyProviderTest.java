package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.tools.StringTools.humanizeClassName;
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
            coppaStudyProtocol("NCI-123", "Official", "Public")
        });
        expect(studySiteClient.getByStudyProtocol((Id) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NCI");

        assertEquals("Incorrect number of studies returned", 1, actual.size());
        assertStudy("Wrong study created", "NCI-123", "Official", actual.get(0));
    }

    public void testSearchWithNoResults() throws Exception{
        expect(client.search((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NOTHING");

        assertTrue("Incorrect number of studies returned", actual.isEmpty());
    }

    public void testCreateStudy() throws Exception {
        expect(studySiteClient.getByStudyProtocol((Id) notNull())).andReturn(new StudySite[] {
            coppaLeadStudySite("Local")
        });
        mocks.replayMocks();

        StudyProtocol sp = coppaStudyProtocol("NCI-123", "Official", "Public");
        Study actual = provider.createStudy(sp);

        assertSecondaryIdentifier(actual, "extension", "NCI-123");
        assertSecondaryIdentifier(actual, "publicTitle", "Public");
        assertSecondaryIdentifier(actual, "officialTitle", "Official");
        assertSecondaryIdentifier(actual, "localStudyProtocolIdentifier", "Local");
    }

    // Helper Methods
    private void assertStudy(String msg, String expectedAssignedId, String expectedLongTitle, Study actual) {
        assertEquals(msg + ": wrong name",  expectedAssignedId,  actual.getAssignedIdentifier());
        assertEquals(msg + ": wrong ident", expectedLongTitle, actual.getLongTitle());
    }

    private void assertSecondaryIdentifier(Study actual, String identifierName, String expectedValue) {
        String human = humanizeClassName(identifierName);
        assertEquals("Wrong " + human, expectedValue, actual.getSecondaryIdentifierValue(identifierName));
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
}
