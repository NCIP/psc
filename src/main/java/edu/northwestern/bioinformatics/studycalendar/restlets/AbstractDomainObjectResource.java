package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.Context;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;

/**
 * Implements GET for a resource backed by a single domain object.
 *
 * @author Rhett Sutphin
 */
public abstract class AbstractDomainObjectResource<D extends DomainObject> extends Resource {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private D requestedObject;
    protected StudyCalendarXmlSerializer<D> xmlSerializer;

    protected abstract D loadRequestedObject(Request request);

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
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(requestedObject);
        } else {
            return null;
        }
    }

    protected Representation createXmlRepresentation(D object) {
        return new StringRepresentation(
            xmlSerializer.createDocumentString(object), MediaType.TEXT_XML);
    }

    ////// CONFIGURATION

    @Required
    public void setXmlSerializer(StudyCalendarXmlSerializer<D> studyCalendarXmlFactory) {
        this.xmlSerializer = studyCalendarXmlFactory;
    }
}
