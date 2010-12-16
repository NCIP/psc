package edu.northwestern.bioinformatics.studycalendar.restlets;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.io.IOException;

/**
 * Base class for resources which are backed by a single domain object and
 * which support GET and PUT.
 *
 * @author Rhett Sutphin
 */
public abstract class AbstractStorableDomainObjectResource<D extends DomainObject> extends AbstractDomainObjectResource<D> {

    @Override public boolean allowPut() { return true; }

    @Override
    @SuppressWarnings({"unchecked"})
    public void storeRepresentation(Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {
            validateEntity(entity);
            D read;
            try {
                read = getXmlSerializer().readDocument(entity.getStream());
                store(read);
            } catch (IOException e) {
                log.warn("PUT failed with IOException", e);
                throw new ResourceException(e);
            }
            getResponse().setEntity(createXmlRepresentation(read));
            if (getRequestedObject() == null) {
                getResponse().setStatus(Status.SUCCESS_CREATED);
            } else {
                getResponse().setStatus(Status.SUCCESS_OK);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    public abstract void store(D instance) throws ResourceException;
    protected void validateEntity(Representation entity) throws ResourceException {}
}
