package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.po.Id;
import gov.nih.nci.coppa.po.IdentifiedOrganization;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.entities.organization.client.OrganizationClient;
import gov.nih.nci.coppa.services.structuralroles.identifiedorganization.client.IdentifiedOrganizationClient;
import gov.nih.nci.coppa.services.structuralroles.researchorganization.client.ResearchOrganizationClient;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import org.iso._21090.*;

import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class CoppaSiteProviderTest extends TestCase {
    private CoppaSiteProvider provider;
    private OrganizationClient client;
    private IdentifiedOrganizationClient iClient;
    private ResearchOrganizationClient rClient;
    private MockRegistry mocks = new MockRegistry();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = mocks.registerMockFor(OrganizationClient.class);
        iClient = mocks.registerMockFor(IdentifiedOrganizationClient.class);
        rClient = mocks.registerMockFor(ResearchOrganizationClient.class);

        provider = new CoppaSiteProvider();
        provider.setOrganizationClient(client);
        provider.setIdentifiedOrganizationClient(iClient);
        provider.setResearchOrganizationClient(rClient);
    }

    public void testNameSearchCriterionIsDelForPartialName() throws Exception {
        Organization crit = provider.createNameExample("quux");
        assertEquals("Should have one part", 1, crit.getName().getPart().size());
        ENXP actualPart = crit.getName().getPart().get(0);
        assertEquals(EntityNamePartType.DEL, actualPart.getType());
        assertEquals("quux", actualPart.getValue());
    }

    public void testGetSiteReturnsNullForUnknown() throws Exception {
        expect(client.search((Organization) notNull())).andReturn(null);
        mocks.replayMocks();

        Site actual = provider.getSite("any");
        assertNull(actual);
    }

    public void testGetSiteReturnsSiteForKnownOrg() throws Exception {
        expect(client.search((Organization) notNull())).andReturn(new Organization[] {
            coppaOrganization("NU", "42")
        });
        mocks.replayMocks();

        Site actual = provider.getSite("42");
        assertSite("Wrong site created", "NU", "42", actual);
    }

    public void testSearchHandlesNullReturnedArray() throws Exception {
        expect(client.search((Organization) notNull())).andReturn(null);
        mocks.replayMocks();

        List<Site> actual = provider.search("foo");
        assertEquals("Should be no results", 0, actual.size());
    }

    public void testSearchReturnsCreatedSites() throws Exception {
        expect(client.search((Organization) notNull())).andReturn(new Organization[] {
            coppaOrganization("NU", "420"),
            coppaOrganization("NA", "422"),
            coppaOrganization("NO", "442")
        });
        mocks.replayMocks();

        List<Site> actual = provider.search("N");
        assertEquals("Wrong number of sites", 3, actual.size());
        assertSite("Wrong site 0", "NU", "420", actual.get(0));
        assertSite("Wrong site 1", "NA", "422", actual.get(1));
        assertSite("Wrong site 2", "NO", "442", actual.get(2));
    }

    public void pending_testGetSitesReturnsCreatedSites() throws Exception {
        expect(iClient.getByPlayerIds((Id[]) notNull())).andReturn(new IdentifiedOrganization[] {
            coppaIdentifiedOrganization("NU", "420"),
        });

        expect(rClient.getByPlayerIds((Id[]) notNull())).andReturn(new ResearchOrganization[] {
            coppaResearchOrganization("NA", "422"),
        });
        mocks.replayMocks();

        List<Site> actual = provider.getSites(asList("420", "422"));
        assertEquals("Wrong number of sites", 2, actual.size());
        assertSite("Wrong site 0", "NU", "420", actual.get(0));
        assertSite("Wrong site 1", "NA", "422", actual.get(1));
    }

    private void assertSite(String msg, String expectedName, String expectedIdent, Site actual) {
        assertEquals(msg + ": wrong name",  expectedName,  actual.getName());
        assertEquals(msg + ": wrong ident", expectedIdent, actual.getAssignedIdentifier());
    }

    private Organization coppaOrganization(String name, String iiValue) {
        Organization org = new Organization();

        ENON n = new ENON();
        ENXP namePart = new ENXP();
        namePart.setType(EntityNamePartType.DEL);
        namePart.setValue(name);
        n.getPart().add(namePart);
        org.setName(n);

        Id id = new Id();
        id.setRoot(OrganizationIdentifier.ORGANIZATION_II_ROOT);
        id.setExtension(iiValue);
        org.setIdentifier(id);

        return org;
    }


    private IdentifiedOrganization coppaIdentifiedOrganization(String name, String idValue) {
        IdentifiedOrganization org = new IdentifiedOrganization();

        II i = new II();
        i.setRoot(OrganizationIdentifier.ORGANIZATION_II_ROOT);
        i.setExtension(idValue);

        org.setAssignedId(i);

        return org;
    }
    
    private ResearchOrganization coppaResearchOrganization(String name, String idValue) {
        ResearchOrganization org = new ResearchOrganization();

        ST st = new ST();
        st.setValue(name);
        org.setName(st);

        II i = new II();
        i.setRoot(OrganizationIdentifier.ORGANIZATION_II_ROOT);
        i.setExtension(idValue);

        org.setPlayerIdentifier(i);

        return org;
    }
}
