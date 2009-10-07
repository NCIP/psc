package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
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

    public void testGetAssociatedSitesWithNoResults() throws Exception {
        expect(client.getByStudyProtocol((Id) EasyMock.notNull())).andReturn(
            new gov.nih.nci.coppa.services.pa.StudySite[0]
        );

        List<List<StudySite>> actual = provider.getAssociatedSites(asList(
            pscStudy("Ext A")
        ));

        assertEquals("Wrong results size", 1, actual.size());
        assertNull("Wrong element", actual.get(0));
    }

    /////////////// Helper Methods
    private gov.nih.nci.coppa.services.pa.StudySite coppaStudySite(String extension) {
        gov.nih.nci.coppa.services.pa.StudySite studySite =
            new gov.nih.nci.coppa.services.pa.StudySite();
        II ii = new II();
        ii.setExtension(extension);
        studySite.setIdentifier(ii);
        return studySite;
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
