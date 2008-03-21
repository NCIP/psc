package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public abstract class AbstractCollectionResource<D extends DomainObject> extends AbstractPscResource {
    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(getAllObjects());
        } else {
            return null;
        }
    }

    protected Representation createXmlRepresentation(Collection<D> instances) {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(instances), MediaType.TEXT_XML);
    }

    public abstract Collection<D> getAllObjects() throws ResourceException;

    public abstract StudyCalendarXmlCollectionSerializer<D> getXmlSerializer();
}
