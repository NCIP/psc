/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.caxchange.MessagePayload;
import gov.nih.nci.caxchange.Metadata;
import org.apache.axis.message.MessageElement;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

/**
 * @author Rhett Sutphin
 */
public class IntegrationHubCoppaTask {
    private HubOperation op;
    private Object[] requestParameters;
    private String identity;

    protected IntegrationHubCoppaTask(HubOperation op, Object... requestParameters) {
        this.op = op;
        this.requestParameters = requestParameters;
        this.identity = UUID.randomUUID().toString();
    }

    ////// LOGIC

    public InputStream getWsddStream() {
        return XmlHelper.getWsddStream(getClientClass());
    }

    public Metadata createMetadata() {
        Metadata md = new Metadata();
        md.setExternalIdentifier(identity);
        md.setOperationName(getHubOperation().getOperationName());
        md.setServiceType(getHubOperation().getServiceType());
        return md;
    }

    public MessageElement[] createRequestPayloadElements() {
        MessageElement[] result = new MessageElement[getRequestParameters().length];
        for (int i = 0; i < getRequestParameters().length; i++) {
            result[i] = new MessageElement(
                XmlHelper.marshalSingleJaxbObject(getRequestParameters()[i]));
        }
        return result;
    }

    public MessagePayload createPayload() {
        return new MessagePayload(
            createRequestPayloadElements(),
            getHubOperation().getNamespaceURI()
        );
    }

    public Object extractResponse(List<MessageElement> elements) throws Exception {
        // If there are ever more than two possibilities here, this should be done
        // polymorphically or with a command object.
        if (getHubOperation().getResponseType().isArray()) {
            return buildResponseArray(elements);
        } else {
            return unmarshalMessageElement(elements.get(0), getHubOperation().getResponseType());
        }
    }

    private Object buildResponseArray(List<MessageElement> elements) throws Exception {
        Class<?> arrayType = getHubOperation().getResponseType();
        try {
            Object array = Array.newInstance(arrayType.getComponentType(), elements.size());
            for (int i = 0; i < elements.size(); i++) {
                MessageElement element = elements.get(i);
                Array.set(array, i, unmarshalMessageElement(element, arrayType.getComponentType()));
            }
            return array;
        } catch (InstantiationException e) {
            throw new StudyCalendarSystemException("Could not instantiate array type %s", arrayType.getName(), e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarSystemException("Could not instantiate array type %s", arrayType.getName(), e);
        }
    }

    private Object unmarshalMessageElement(MessageElement element, Class<?> type) throws Exception {
        return XmlHelper.unmarshalSingleJaxbObject(element.getAsDOM(), type);
    }

    ////// ACCESSORS

    public HubOperation getHubOperation() {
        return op;
    }

    public Object[] getRequestParameters() {
        return requestParameters;
    }

    public Class<?> getClientClass() {
        return getHubOperation().getClientClass();
    }

    public String getIdentity() {
        return identity;
    }
}
