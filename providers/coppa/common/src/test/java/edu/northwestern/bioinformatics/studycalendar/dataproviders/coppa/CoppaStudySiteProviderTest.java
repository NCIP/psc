/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addSecondaryIdentifier;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.po.HealthCareFacility;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.services.pa.Id;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import org.iso._21090.*;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.sort;
import java.util.List;

/**
 * @author John Dzak
 */
public class CoppaStudySiteProviderTest extends TestCase {
    private CoppaStudySiteProvider provider;
    private MockRegistry mocks = new MockRegistry();
    private CoppaAccessor coppaAccessor;
    private BundleContext bundleContext;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundleContext = mocks.registerMockFor(BundleContext.class);
        coppaAccessor = mocks.registerMockFor(CoppaAccessor.class);

        MockServiceReference ref = new MockServiceReference();
        expect(bundleContext.getServiceReference(CoppaProviderHelper.ACCESSOR_SERVICE)).
            andStubReturn(ref);
        expect(bundleContext.getService(ref)).andStubReturn(coppaAccessor);


        provider = new CoppaStudySiteProvider(bundleContext);
    }

    public void testIsValidExtension() {
        boolean result = provider.isValidExtension("123");
        assertTrue("Should be valid", result);
    }

    public void testIsValidExtensionWithNullExtension() {
        boolean result = provider.isValidExtension(null);
        assertFalse("Should not be valid", result);
    }

    public void testIsValidExtensionWithInvalidExtension() {
        boolean result = provider.isValidExtension("INVALID");
        assertFalse("Should not be valid", result);
    }


    public void testGetAssociatedSitesWithEmptyStudyList() throws Exception {
        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(EMPTY_LIST);
        mocks.verifyMocks();

        assertEquals("Wrong results size", 0, actual.size());
    }

    public void testGetAssociatedSitesWithNoResults() throws Exception {
        expectSearchStudySitesByStudyProcotolId();

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(asList(
            pscStudy("NU123")
        ));
        mocks.verifyMocks();

        assertEquals("Wrong results size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedSitesWithNoRoleResults() throws Exception {
        expectSearchStudySitesByStudyProcotolId(
            coppaStudySite("NU <-> NCI123", "NCI123", "Health Care Facility (NU)")
        );

        expectGetHealthCareFacilities();


        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(pscStudy("NU123"))
        );
        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedSitesWithNullRoleResults() throws Exception {
        expectSearchStudySitesByStudyProcotolId(
            coppaStudySite("NU <-> NCI123", "NCI123", "Health Care Facility (NU)")
        );

        expectGetHealthCareFacilities(null);


        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(pscStudy("NCI123"))
        );
        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedSitesWithMultipleResults() throws Exception {
        expectSearchStudySitesByStudyProcotolId(
            coppaStudySite("NU <-> NCI123", "NCI123", "Health Care Facility (NU)")
        );

        expectGetHealthCareFacilities(
            coppaHealthCareFacility("Health Care Facility (NU)", "NU"),
            coppaHealthCareFacility("Health Care Facility (Mayo)", "Mayo")
        );

        expectGetOrganization(coppaOrganization("NU"));

        expectGetOrganization(coppaOrganization("Mayo"));

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(pscStudy("NCI123"))
        );
        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 2, actual.get(0).size());

        Site actualSite0 = actual.get(0).get(0).getSite();
        assertEquals("Wrong name", "NU", actualSite0.getName());
        assertEquals("Wrong name", extensionify("NU"), actualSite0.getAssignedIdentifier());

        Site actualSite1 = actual.get(0).get(1).getSite();
        assertEquals("Wrong name", "Mayo", actualSite1.getName());
        assertEquals("Wrong name", extensionify("Mayo"), actualSite1.getAssignedIdentifier());
    }

    public void testGetAssociatedSitesWithNullStudy() {
        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(null);
        mocks.verifyMocks();

        assertEquals("Wrong size", 0, actual.size());
    }

    public void testGetAssociatedSitesWithBlankStudy() {
        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(new Study())
        );
        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedSitesWithANonNumericExtension() {
        Study brokenStudy = new Study();
        addSecondaryIdentifier(brokenStudy, COPPA_STUDY_IDENTIFIER_TYPE, "YOU BETTER NOT SEARCH COPPA WITH ME!!!");

        mocks.replayMocks();
        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(brokenStudy)
        );

        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedStudiesWithBothRoleResults() throws Exception {
        expectGetHealthCareFacilitiesByPlayerIds(
            coppaHealthCareFacility("Health Care Facility (NU)", "NU")
        );

        expectSearchStudySitesByStudySite(
            coppaStudySite("NU <-> NU123", "NU123", "Health Care Facility (NU)"),
            coppaStudySite("NU <-> NU999", "NU999", "Health Care Facility (NU)")
        );

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedStudies(
            asList(pscSite("NU"))
        );

        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 2, actual.get(0).size());

        List<String> secondaryIds = new ArrayList<String>();
        secondaryIds.add(actual.get(0).get(0).getStudy().getSecondaryIdentifierValue(COPPA_STUDY_IDENTIFIER_TYPE));
        secondaryIds.add(actual.get(0).get(1).getStudy().getSecondaryIdentifierValue(COPPA_STUDY_IDENTIFIER_TYPE));
        sort(secondaryIds);

        assertEquals("Wrong name", extensionify("NU123"), secondaryIds.get(0));
        assertEquals("Wrong name", extensionify("NU999"), secondaryIds.get(1));
    }

    public void testGetAssociatedStudiesWithNoRoleResults() throws Exception {
        expectGetHealthCareFacilitiesByPlayerIds(null);

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedStudies(
            asList(pscSite("NU"))
        );

        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }


    public void testGetAssociatedStudiesWithNoSite() throws Exception {
        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedStudies(null);

        mocks.verifyMocks();

        assertEquals("Wrong size", 0, actual.size());
    }

    public void testGetAssociatedStudiesWithSiteMissingAssignedIdentifier() throws Exception {
        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedStudies(
            asList(new Site())
        );

        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedStudiesWithANonNumericExtension() {
        Site brokenSite = createSite(null, "YOU BETTER NOT SEARCH COPPA WITH ME!!!");

        mocks.replayMocks();
        List<List<StudySite>> actual = provider.getAssociatedStudies(asList(brokenSite));

        mocks.verifyMocks();

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }


    /////////////// Expect Methods
    private void expectSearchStudySitesByStudyProcotolId(gov.nih.nci.coppa.services.pa.StudySite... s) {
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(s);
    }

    private void expectGetHealthCareFacilities(HealthCareFacility... h) {
        expect(coppaAccessor.getHealthCareFacilities((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn(h);
    }

    private void expectGetOrganization(Organization o) {
        expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(o);
    }

    private void expectSearchStudySitesByStudySite(gov.nih.nci.coppa.services.pa.StudySite... s) {
        expect(coppaAccessor.searchStudySitesByStudySite((gov.nih.nci.coppa.services.pa.StudySite) notNull(), (LimitOffset) notNull())).andReturn(s);
    }

    private void expectGetHealthCareFacilitiesByPlayerIds(HealthCareFacility... h) {
        expect(coppaAccessor.getHealthCareFacilitiesByPlayerIds((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn(h);
    }

    /////////////// Helper Methods
    private gov.nih.nci.coppa.services.pa.StudySite coppaStudySite(String ext, String studyExt, String researchOrgExt) {
        ext = extensionify(ext); studyExt = extensionify(studyExt); researchOrgExt = extensionify(researchOrgExt);

        gov.nih.nci.coppa.services.pa.StudySite studySite =
            new gov.nih.nci.coppa.services.pa.StudySite();

        II ii = new II();
        ii.setExtension(ext);
        studySite.setIdentifier(ii);

        II studyII = new II();
        studyII.setExtension(studyExt);
        studySite.setStudyProtocolIdentifier(studyII);

        II roII = new II();
        roII.setExtension(researchOrgExt);
        studySite.setResearchOrganization(roII);

        return studySite;
    }

    private HealthCareFacility coppaHealthCareFacility(String ext, String playerExt) {
        ext = extensionify(ext); playerExt = extensionify(playerExt);

        HealthCareFacility h = new HealthCareFacility();

        DSETII dsetti = new DSETII();
        II ii = new II();
        ii.setExtension(ext);
        ii.setIdentifierName("NCI Health Care Facility identifier");
        dsetti.getItem().add(ii);

        II playerII = new II();
        playerII.setExtension(playerExt);

        h.setIdentifier(dsetti);
        h.setPlayerIdentifier(playerII);

        return h;
    }

    private Organization coppaOrganization(String name) {
        Organization org = new Organization();

        ENON n = new ENON();
        ENXP namePart = new ENXP();
        namePart.setType(EntityNamePartType.DEL);
        namePart.setValue(name);
        n.getPart().add(namePart);
        org.setName(n);

        gov.nih.nci.coppa.po.Id id = new gov.nih.nci.coppa.po.Id();
        id.setRoot("ROOT");
        id.setExtension(extensionify(name));
        org.setIdentifier(id);

        return org;
    }

    private Study pscStudy(String copppaStudyExtension) {
        Study study = new Study();
        addSecondaryIdentifier(study, COPPA_STUDY_IDENTIFIER_TYPE, extensionify(copppaStudyExtension));

        return study;
    }

    private Site pscSite(String identifier) {
        return createSite(null, extensionify(identifier));
    }

    private String extensionify(String s) {
        Integer sum = 0;
        if (s != null) {
            for (char c : s.toCharArray()) {
                sum += (int) c;
            }
        }
        return sum.toString();
    }
}
