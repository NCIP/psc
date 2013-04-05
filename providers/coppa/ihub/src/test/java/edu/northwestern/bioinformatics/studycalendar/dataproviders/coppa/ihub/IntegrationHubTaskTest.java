/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import junit.framework.TestCase;
import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for the abstract behavior in {@link IntegrationHubCoppaTask}.
 *
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
public class IntegrationHubTaskTest extends TestCase {
    public void testDefaultIdentityIsNotNull() throws Exception {
        assertNotNull(new IntegrationHubCoppaTask(PaOperation.STUDY_PROTOCOL_SEARCH).getIdentity());
    }

    public void testMetadataHasServiceType() throws Exception {
        IntegrationHubCoppaTask task = new IntegrationHubCoppaTask(PaOperation.STUDY_PROTOCOL_SEARCH);
        assertEquals("STUDY_PROTOCOL", task.createMetadata().getServiceType());
    }

    public void testMetadataHasOperationName() throws Exception {
        IntegrationHubCoppaTask task = new IntegrationHubCoppaTask(PaOperation.STUDY_PROTOCOL_SEARCH);
        assertEquals("search", task.createMetadata().getOperationName());
    }

    public void testMetadataHasExternalId() throws Exception {
        IntegrationHubCoppaTask task = new IntegrationHubCoppaTask(PaOperation.GET_STUDY_PROTOCOL);
        assertEquals(task.getIdentity(), task.createMetadata().getExternalIdentifier());
    }

    // this is a pretty crappy test
    public void testCreateRequestPayloadElementWorks() throws Exception {
        StudyProtocol expectedCriteria = createStudyProtocol("12345", "not a real id, but pretending");
        LimitOffset expectedLimit = new LimitOffset();
        expectedLimit.setLimit(15);
        expectedLimit.setOffset(0);

        IntegrationHubCoppaTask task = new IntegrationHubCoppaTask(
            PaOperation.STUDY_PROTOCOL_SEARCH, expectedCriteria, expectedLimit);

        MessageElement[] actual = task.createRequestPayloadElements();
        assertNotNull(actual);
        assertEquals("Wrong number of elements", 2, actual.length);
        Element dom = actual[0].getAsDOM();
        assertEquals("Wrong first element", "StudyProtocol", dom.getLocalName());
        assertEquals("Wrong namespace for element",
            PaOperation.STUDY_PROTOCOL_SEARCH.getNamespaceURI().toString(), dom.getNamespaceURI());
        dom = actual[1].getAsDOM();
        assertEquals("Wrong second element", "LimitOffset", dom.getLocalName());
    }

    public void testExtractingSingleResponseWorks() throws Exception {
        StudyProtocol study = createStudyProtocol("A", "42");
        MessageElement protocolResponse =
            new MessageElement(XmlHelper.marshalSingleJaxbObject(study));

        IntegrationHubCoppaTask task = new IntegrationHubCoppaTask(
            PaOperation.GET_STUDY_PROTOCOL, study.getAssignedIdentifier());

        StudyProtocol extracted = (StudyProtocol) task.extractResponse(Arrays.asList(protocolResponse));
        assertNotNull("Nothing extracted", extracted);
        assertEquals("Wrong thing extracted", "42", extracted.getAssignedIdentifier().getExtension());
    }

    public void testExtractingMultipleResponsesWorks() throws Exception {
        StudyProtocol in = createStudyProtocol("A", "42");

        List<MessageElement> response = Arrays.asList(
            new MessageElement(XmlHelper.marshalSingleJaxbObject(createStudyProtocol("A", "420"))),
            new MessageElement(XmlHelper.marshalSingleJaxbObject(createStudyProtocol("A", "421")))
        );

        IntegrationHubCoppaTask task = new IntegrationHubCoppaTask(
            PaOperation.STUDY_PROTOCOL_SEARCH, in);

        StudyProtocol[] extracted = (StudyProtocol[]) task.extractResponse(response);
        assertNotNull("Nothing extracted", extracted);
        assertEquals("Wrong number of things extracted", 2, extracted.length);
        assertEquals("Wrong 1st thing extracted", "420", extracted[0].getAssignedIdentifier().getExtension());
        assertEquals("Wrong 2nd thing extracted", "421", extracted[1].getAssignedIdentifier().getExtension());
    }

    private static StudyProtocol createStudyProtocol(String root, String ext) {
        Id expectedId = new Id();
        expectedId.setRoot(root);
        expectedId.setExtension(ext);
        StudyProtocol expectedCriteria = new StudyProtocol();
        expectedCriteria.setAssignedIdentifier(expectedId);
        return expectedCriteria;
    }
}
