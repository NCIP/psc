package edu.northwestern.bioinformatics.studycalendar.restlets;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * @author John Dzak
 */
public abstract class AbstractRemovableStorableDomainObjectResource<D extends DomainObject> extends AbstractStorableDomainObjectResource<D>  {

    @Override
    public void doInit() {
        super.doInit();
        getAllowedMethods().add(Method.DELETE);
    }

    @Override
    public Representation delete(Variant variant) throws ResourceException {
        verifyRemovable(getRequestedObject());
        remove(getRequestedObject());
        
        getResponse().setStatus(Status.SUCCESS_OK);
        return null;
    }

    public abstract void remove(D instance);
    public void verifyRemovable(D instance) throws ResourceException { }
}
