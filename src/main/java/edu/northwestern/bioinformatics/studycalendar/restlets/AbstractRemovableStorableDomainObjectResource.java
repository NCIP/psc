package edu.northwestern.bioinformatics.studycalendar.restlets;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * @author John Dzak
 */
public abstract class AbstractRemovableStorableDomainObjectResource<D extends DomainObject> extends AbstractStorableDomainObjectResource<D>  {

    @Override public boolean allowDelete() { return true; }

    @Override
    public void removeRepresentations() throws ResourceException {
        D retreived = getRequestedObject();
        remove(retreived);

        if (getRequestedObject() == null) {
            getResponse().setStatus(Status.SUCCESS_OK);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    public abstract void remove(D instance);
}
