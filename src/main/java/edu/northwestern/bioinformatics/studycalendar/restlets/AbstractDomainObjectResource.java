package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Implements GET for a resource backed by a single domain object.
 *
 * @author Rhett Sutphin
 */
public abstract class AbstractDomainObjectResource<D extends DomainObject> extends AbstractPscResource {
    private D requestedObject;
    protected StudyCalendarXmlSerializer<D> xmlSerializer;
    private String clientErrorReason;

    /**
     * Load the domain object which corresponds to the requested resource.  If
     * there isn't one, this method must return null.
     */
    protected abstract D loadRequestedObject(Request request);

    /**
     * Allows subclasses to expand upon the reason why the request failed.  E.g., "no amendment with
     * that key," etc.
     *
     * @param reason
     */
    protected void setClientErrorReason(String reason, String... params) {
        clientErrorReason = String.format(reason, (Object[]) params);
        log.debug("client error: {}", clientErrorReason);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        setModifiable(false);
        getVariants().add(new Variant(MediaType.TEXT_XML));

        requestedObject = loadRequestedObject(request);
        setAvailable(requestedObject != null);
        if (isAvailable()) {
            log.debug("Request {} maps to {}", request.getResourceRef(), requestedObject);
        } else {
            log.debug("Request {} does not map to an existing domain object", request.getResourceRef());
        }
    }

    public D getRequestedObject() {
        return requestedObject;
    }

    @Override
    public void handleGet() {
        super.handleGet();
        if (getResponse().getStatus().isClientError() && getResponse().getEntity() == null) {
            getResponse().setEntity(new StringRepresentation(
                createDefaultClientErrorEntity(getResponse().getStatus()), MediaType.TEXT_PLAIN));
        }
    }

    private StringBuilder createDefaultClientErrorEntity(Status status) {
        StringBuilder message = new StringBuilder().
            append(status.getCode()).append(' ').append(status.getName());
        if (status.getDescription() != null) {
            message.append(": ").append(status.getDescription());
        }
        if (clientErrorReason != null) {
            message.
                append("\n\n").
                append(clientErrorReason);
        }
        message.append('\n');
        return message;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(requestedObject);
        } else {
            return null;
        }
    }

    protected Representation createXmlRepresentation(D object) {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(object), MediaType.TEXT_XML);
    }

    ////// CONFIGURATION

    public StudyCalendarXmlSerializer<D> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlSerializer<D> studyCalendarXmlFactory) {
        this.xmlSerializer = studyCalendarXmlFactory;
    }
}
