/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/**
 * Implements GET for a resource backed by a single domain object.
 *
 * @author Rhett Sutphin
 */
public abstract class AbstractDomainObjectResource<D extends DomainObject> extends AbstractPscResource {
    private D requestedObject;
    protected StudyCalendarXmlSerializer<D> xmlSerializer;
    private ResourceException objectLoadException;

    /**
     * Load the domain object which corresponds to the requested resource.  If
     * there isn't one, this method must return null.
     * @throws ResourceException when some unusual condition is met.  This method should
     *   not throw ResourceException for 404s, but rather for 403s and similar conditions
     *   where you need to short-circuit the usual domain object loading process.
     */
    protected abstract D loadRequestedObject(Request request) throws ResourceException;

    @Override
    public void doInit() {
        super.doInit();
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getAllowedMethods().add(Method.GET);

        try {
            requestedObject = loadRequestedObject(getRequest());
        } catch (ResourceException e) {
            // Defer throwing exception until the subclass actually attempts to access the object
            objectLoadException = e;
        }
        setExisting(requestedObject != null || objectLoadException != null);
        if (requestedObject != null) {
            log.debug("Request {} maps to {}", getRequest().getResourceRef(), requestedObject);
        } else {
            log.debug("Request {} does not map to an existing domain object",
                getRequest().getResourceRef());
        }
    }

    public D getRequestedObject() throws ResourceException {
        if (objectLoadException != null) {
            throw objectLoadException;
        } else {
            return requestedObject;
        }
    }

    /**
     * Returns the requested object if there is one, but continues to defer throwing the
     * resource exception until outside of init.
     */
    protected D getRequestedObjectDuringInit() {
        return requestedObject;
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(getRequestedObject());
        } else if (variant.getMediaType() == MediaType.TEXT_CALENDAR) {
            return createCalendarRepresentation(getRequestedObject());
        } else if (variant.getMediaType() == MediaType.APPLICATION_JSON) {
            return createJsonRepresentation(getRequestedObject());
        } else {
            return null;
        }
    }

    protected Representation createCalendarRepresentation(D object) {
        return null;

    }

    protected Representation createJsonRepresentation(D object) {
        return null;
    }

    protected Representation createXmlRepresentation(D object) throws ResourceException {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(object), MediaType.TEXT_XML);
    }

    ////// CONFIGURATION

    public StudyCalendarXmlSerializer<D> getXmlSerializer() throws ResourceException {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlSerializer<D> studyCalendarXmlFactory) {
        this.xmlSerializer = studyCalendarXmlFactory;
    }
}
