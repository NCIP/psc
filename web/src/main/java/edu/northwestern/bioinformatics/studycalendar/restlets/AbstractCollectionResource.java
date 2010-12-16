package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public abstract class AbstractCollectionResource<D extends DomainObject> extends AbstractPscResource {
    @Override
    public void doInit() {
        super.doInit();
        getAllowedMethods().add(Method.GET);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
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
