package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.po.HealthCareFacility;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.pa.Id;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import org.iso._21090.*;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.ArrayList;
import static java.util.Arrays.asList;
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

    public void testGetAssociatedSitesWithEmptyStudyList() throws Exception {
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(
            new gov.nih.nci.coppa.services.pa.StudySite[0]
        );

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(new ArrayList<Study>());

        assertEquals("Wrong results size", 0, actual.size());
    }

    public void testGetAssociatedSitesWithNoResults() throws Exception {
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(
            new gov.nih.nci.coppa.services.pa.StudySite[0]
        );

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(asList(
            pscStudy("Ext A")
        ));

        assertEquals("Wrong results size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedSitesWithOnlyResearchOrgResults() throws Exception {
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn( new gov.nih.nci.coppa.services.pa.StudySite[] {
            coppaStudySite("Ext SS", null, "Ext RO")
        });

        expect(coppaAccessor.getResearchOrganizations((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn( new ResearchOrganization[] {
            coppaResearchOrg("Ext RO A", "Player Ext RO A"),
            coppaResearchOrg("Ext RO B", "Player Ext RO B")
        });

        expect(coppaAccessor.getHealthCareFacilities((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn( new HealthCareFacility[0]);

        expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
            coppaOrganization("Name A", "Ext O A")
        );            

        expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
            coppaOrganization("Name B", "Ext O B")
        );

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(pscStudy("Ext SS"))
        );

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 2, actual.get(0).size());
        assertEquals("Wrong name", "Name A", actual.get(0).get(0).getSite().getName());
        assertEquals("Wrong name", "Name B", actual.get(0).get(1).getSite().getName());
    }

    public void testGetAssociatedSitesWithBothResearchOrgAndHealthCareFacilityResults() throws Exception {
        expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn( new gov.nih.nci.coppa.services.pa.StudySite[] {
            coppaStudySite("Ext SS", null, "Ext RO")
        });

        expect(coppaAccessor.getResearchOrganizations((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn( new ResearchOrganization[] {
            coppaResearchOrg("Ext RO A", "Player Ext RO A"),
        });

        expect(coppaAccessor.getHealthCareFacilities((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn( new HealthCareFacility[] {
            coppaHealthCareFacility("Ext HCF A", "Player Ext HCF A"),
        });

        expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
            coppaOrganization("Name A", "Ext O A")
        );

        expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
            coppaOrganization("Name B", "Ext O B")
        );

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(pscStudy("Ext SS"))
        );

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 2, actual.get(0).size());
        
        assertEquals("Wrong name", "Name A", actual.get(0).get(0).getSite().getName());
        assertEquals("Wrong name", "Ext O A", actual.get(0).get(0).getSite().getAssignedIdentifier());

        assertEquals("Wrong name", "Name B", actual.get(0).get(1).getSite().getName());
        assertEquals("Wrong name", "Ext O B", actual.get(0).get(1).getSite().getAssignedIdentifier());
    }

    public void testGetAssociatedSitesWithBlankStudy() {
        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(new Study())
        );

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 0, actual.get(0).size());
    }

    public void testGetAssociatedStudiesWithResults() throws Exception {
        expect(coppaAccessor.getResearchOrganizationsByPlayerIds((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn( new ResearchOrganization[] {
                coppaResearchOrg("Resesarch Org (NU)", "NU"),
        });

        expect(coppaAccessor.searchStudySitesByStudySite((gov.nih.nci.coppa.services.pa.StudySite) notNull(), (LimitOffset) notNull())).andReturn( new gov.nih.nci.coppa.services.pa.StudySite[] {
            coppaStudySite("NU <-> NU123", "NU123", "Resesarch Org (NU)")
        });

        mocks.replayMocks();

        List<List<StudySite>> actual = provider.getAssociatedStudies(
            asList(pscSite("NU"))
        );

        assertEquals("Wrong size", 1, actual.size());
        assertEquals("Wrong size", 1, actual.get(0).size());
        assertEquals("Wrong name", "NU123", actual.get(0).get(0).getStudy().getSecondaryIdentifierValue(COPPA_STUDY_IDENTIFIER_TYPE));
    }


    /////////////// Helper Methods
    private gov.nih.nci.coppa.services.pa.StudySite coppaStudySite(String extension, String studyExtension, String researchOrgExtension) {
        gov.nih.nci.coppa.services.pa.StudySite studySite =
            new gov.nih.nci.coppa.services.pa.StudySite();

        II ii = new II();
        ii.setExtension(extension);
        studySite.setIdentifier(ii);

        II studyII = new II();
        studyII.setExtension(studyExtension);
        studySite.setStudyProtocolIdentifier(studyII);

        II roII = new II();
        roII.setExtension(researchOrgExtension);
        studySite.setResearchOrganization(roII);

        return studySite;
    }

    private ResearchOrganization coppaResearchOrg(String extension, String playerExtension) {
        ResearchOrganization ro = new ResearchOrganization();

        DSETII dsetti = new DSETII();
        II ii = new II();
        ii.setExtension(extension);
        ii.setIdentifierName("NCI Research Organization identifier");
        dsetti.getItem().add(ii);

        II playerII = new II();
        playerII.setExtension(playerExtension);

        ro.setIdentifier(dsetti);
        ro.setPlayerIdentifier(playerII);

        return ro;
    }

    private HealthCareFacility coppaHealthCareFacility(String ext, String playerExt) {
        HealthCareFacility h = new HealthCareFacility();

        DSETII d = new DSETII();
        d.setControlActExtension(ext);
        h.setIdentifier(d);

        II i = new II();
        i.setExtension(playerExt);
        h.setPlayerIdentifier(i);

        return h;
    }

    private Study pscStudy(String extensionSecondaryIdentifier) {
        Study study = new Study();

        StudySecondaryIdentifier i = new StudySecondaryIdentifier ();
        i.setType(COPPA_STUDY_IDENTIFIER_TYPE);
        i.setValue(extensionSecondaryIdentifier);
        study.addSecondaryIdentifier(i);

        return study;
    }

    private Site pscSite(String identifier) {
        Site site = new Site();
        site.setAssignedIdentifier(identifier);
        return site;
    }

     private Organization coppaOrganization(String name, String iiValue) {
        Organization org = new Organization();

        ENON n = new ENON();
        ENXP namePart = new ENXP();
        namePart.setType(EntityNamePartType.DEL);
        namePart.setValue(name);
        n.getPart().add(namePart);
        org.setName(n);

        gov.nih.nci.coppa.po.Id id = new gov.nih.nci.coppa.po.Id();
        id.setRoot("ROOT");
        id.setExtension(iiValue);
        org.setIdentifier(id);

        return org;
    }
}
