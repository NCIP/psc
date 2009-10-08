package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import gov.nih.nci.coppa.services.structuralroles.researchorganization.client.ResearchOrganizationClient;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import org.iso._21090.DSETII;
import org.iso._21090.II;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author John Dzak
 */
public class CoppaStudySiteProviderTest extends TestCase {
    private CoppaStudySiteProvider provider;
    private StudySiteServiceClient client;
    private MockRegistry mocks = new MockRegistry();
    private ResearchOrganizationClient researchOrgClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = mocks.registerMockFor(StudySiteServiceClient.class);

        provider = new CoppaStudySiteProvider();
        provider.setClient(client);
    }

    public void testGetAssociatedSitesWithEmptyStudyList() throws Exception {
        expect(client.getByStudyProtocol((Id) notNull())).andReturn(
            new gov.nih.nci.coppa.services.pa.StudySite[0]
        );

        List<List<StudySite>> actual = provider.getAssociatedSites(new ArrayList<Study>());

        assertEquals("Wrong results size", 0, actual.size());
    }

    public void testGetAssociatedSitesWithNoResults() throws Exception {
        expect(client.getByStudyProtocol((Id) notNull())).andReturn(
            new gov.nih.nci.coppa.services.pa.StudySite[0]
        );

        List<List<StudySite>> actual = provider.getAssociatedSites(asList(
            pscStudy("Ext A")
        ));

        assertEquals("Wrong results size", 1, actual.size());
        assertNull("Wrong element", actual.get(0));
    }

//    public void testGetAssociatedSitesWithResults() throws Exception {
//        expect(client.getByStudyProtocol((Id) notNull())).andReturn( new gov.nih.nci.coppa.services.pa.StudySite[] {
//                coppaStudySite("Ext SS", coppaResearchOrg("Ext RO", "Player Ext RO"))
//            }
//        );
//
//        expect(researchOrgClient.getById((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
//            coppaResearchOrg("Ext RO", "Player Ext RO")
//        );
//
//
//    }

    /////////////// Helper Methods
    private gov.nih.nci.coppa.services.pa.StudySite coppaStudySite(String extension, ResearchOrganization organizations) {
        gov.nih.nci.coppa.services.pa.StudySite studySite =
            new gov.nih.nci.coppa.services.pa.StudySite();

        II ii = new II();
        ii.setExtension(extension);
        studySite.setIdentifier(ii);

        studySite.setResearchOrganization(organizations.getPlayerIdentifier());
        return studySite;
    }

    private ResearchOrganization coppaResearchOrg(String extension, String playerExtension) {
        ResearchOrganization ro = new ResearchOrganization();

        DSETII dsetti = new DSETII();
        dsetti.setControlActExtension(extension);

        II ii = new II();
        ii.setExtension(playerExtension);

        ro.setPlayerIdentifier(ii);

        return ro;
    }

    private Study pscStudy(String extensionSecondaryIdentifier) {
        Study study = new Study();

        StudySecondaryIdentifier i = new StudySecondaryIdentifier ();
        i.setType("extension");
        i.setValue(extensionSecondaryIdentifier);
        study.addSecondaryIdentifier(i);

        return study;
    }
}
