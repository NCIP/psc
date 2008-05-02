package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.Collection;

/**
 * Extends the AbstractStorableCollectionResource and provide the POST functionality for a collection of objects
 *
 * @author Saurabh Agrawal
 */
public abstract class AbstractStorableCollectionResource<D extends DomainObject> extends AbstractCollectionResource {

    @Override
    public boolean allowPost() {
        return true;
    }


    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return new StringRepresentation(getXmlSerializer().createDocumentString(getAllObjects()), MediaType.TEXT_XML);
        } else {
            return null;
        }
    }

    public void acceptRepresentation(final Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {
            validateEntity(entity);
            Collection<D> read;
            try {
                read = getXmlSerializer().readCollectionDocument(entity.getStream());
                store(read);
            } catch (IOException e) {
                log.warn("PUT failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {

                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());

            }

            getResponse().setEntity(createXmlRepresentation(read));
            if (getAllObjects() == null) {
                getResponse().setStatus(Status.SUCCESS_CREATED);
            } else {
                getResponse().setStatus(Status.SUCCESS_OK);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }


    public abstract void store(Collection<D> instances);

    protected void validateEntity(Representation entity) throws ResourceException {
    }


}
