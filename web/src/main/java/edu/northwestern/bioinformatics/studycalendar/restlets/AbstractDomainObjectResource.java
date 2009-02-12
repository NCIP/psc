package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
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

    /**
     * Load the domain object which corresponds to the requested resource.  If
     * there isn't one, this method must return null.
     */
    protected abstract D loadRequestedObject(Request request);

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        setModifiable(false);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));

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
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(requestedObject);
        } else if (variant.getMediaType() == MediaType.TEXT_CALENDAR) {
            return createCalendarRepresentation(requestedObject);
        } else {
            return null;
        }
    }

    protected Representation createCalendarRepresentation(D object) {
        return null;

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
