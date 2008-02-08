package edu.northwestern.bioinformatics.studycalendar.restlets;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * Base class for resources which are backed by a single domain object and
 * which support GET, PUT and DELETE
 *
 * @author Saurabh Agrawal
 */
public abstract class AbstractRemovableDomainObjectResource<D extends DomainObject> extends AbstractStorableDomainObjectResource<D> {


    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void removeRepresentations() throws ResourceException {

        if (getRequestedObject() == null) {
            //throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
            log.error("DELETE failed because no object found for given request");
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        } else {
            try {
                remove(getRequestedObject());
                getResponse().setStatus(Status.SUCCESS_OK);

            } catch (Exception e) {
                log.error("Error while removing object :" + getRequestedObject().getClass(), e);
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
            }

        }

    }


    public abstract void remove(D instance) throws Exception;
}
