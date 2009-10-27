package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import static edu.northwestern.bioinformatics.studycalendar.tools.StringTools.humanizeClassName;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.iso._21090.CD;
import org.iso._21090.II;
import org.iso._21090.ST;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author John Dzak
 */
public class CoppaStudyProviderTest extends TestCase{
    private CoppaStudyProvider provider;
    private CoppaAccessor coppaAccessor;
    private BundleContext bundleContext;

    private MockRegistry mocks = new MockRegistry();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundleContext = mocks.registerMockFor(BundleContext.class);
        coppaAccessor = mocks.registerMockFor(CoppaAccessor.class);

        MockServiceReference ref = new MockServiceReference();
        expect(bundleContext.getServiceReference(CoppaProviderHelper.ACCESSOR_SERVICE)).
            andStubReturn(ref);
        expect(bundleContext.getService(ref)).andStubReturn(coppaAccessor);

        provider = new CoppaStudyProvider(bundleContext);
    }

    public void testSearchWithOneResult() throws Exception{
        expect(coppaAccessor.searchStudyProtocols((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(new StudyProtocol[]{
            coppaStudyProtocol("NCI-123", "Official", "Public")
        });
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NCI");

        assertEquals("Incorrect number of studies returned", 1, actual.size());
        assertStudy("Wrong study created", "NCI-123", "Official", actual.get(0));
    }

    public void testSearchWithNoResults() throws Exception{
        expect(coppaAccessor.searchStudyProtocols((StudyProtocol) notNull(), (LimitOffset) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Study> actual = provider.search("NOTHING");

        assertTrue("Incorrect number of studies returned", actual.isEmpty());
    }

    public void testGetStudies() throws Exception {
        expect(coppaAccessor.getStudyProtocol((Id) notNull())).andReturn(coppaStudyProtocol("Ext A", "Off A", "Pub A"));
        expect(coppaAccessor.getStudyProtocol((Id) notNull())).andReturn(null);
        expect(coppaAccessor.getStudyProtocol((Id) notNull())).andReturn(coppaStudyProtocol("Ext C", "Off C", "Pub C"));

        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(null).times(3);
        mocks.replayMocks();

        List<Study> actual = provider.getStudies(asList(
            pscStudy("Ext A"),
            pscStudy("Ext B"),
            pscStudy("Ext C")
        ));

        assertEquals("Wrong number of elements", 3, actual.size());
        assertNull  ("Should be null", actual.get(1));
        assertEquals("Wrong element", "Ext A", actual.get(0).getAssignedIdentifier());
        assertEquals("Wrong element", "Ext C", actual.get(2).getAssignedIdentifier());
    }

    public void testCreateStudy() throws Exception {
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(new StudySite[] {
            coppaLeadStudySite("Local")
        });
        mocks.replayMocks();

        StudyProtocol sp = coppaStudyProtocol("NCI-123", "Official", "Public");
        Study actual = provider.createStudy(sp);

        assertSecondaryIdentifier(actual, CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE, "NCI-123");
        assertSecondaryIdentifier(actual, "Public Title", "Public");
        assertSecondaryIdentifier(actual, "Official Title", "Official");
        assertSecondaryIdentifier(actual, "Lead Organization Identifier", "Local");
    }

    public void testDetectWithStudyFound() {
        Study actual = provider.detect(pscStudy("Ext B"), asList(
            pscStudy("Ext A"),
            pscStudy("Ext B"),
            pscStudy("Ext C")
        ));

        assertEquals("Wrong Study", "Ext B", actual.getSecondaryIdentifierValue("COPPA Identifier"));
    }
    
    public void testDetectWithStudyNotFound() {
        Study actual = provider.detect(pscStudy("Ext B"), asList(
            pscStudy("Ext Y"),
            pscStudy("Ext Z")
        ));

        assertNull("Should be null", actual);
    }

    ///////////////// Helper Methods
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

        ii.setExtension(extension);
        p.setIdentifier(ii);

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

    private Study pscStudy(String coppaExtension) {
        Study study = new Study();

        StudySecondaryIdentifier id = new StudySecondaryIdentifier();
        id.setType(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
        id.setValue(coppaExtension);
        study.addSecondaryIdentifier(id);

        StudySecondaryIdentifier ext = new StudySecondaryIdentifier();
        ext.setType("Extension");
        ext.setValue(coppaExtension);
        study.addSecondaryIdentifier(ext);

        return study;
    }
}
