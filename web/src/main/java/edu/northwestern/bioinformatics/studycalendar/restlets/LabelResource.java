package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.LabelService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.Context;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.resource.StringRepresentation;
import org.restlet.data.*;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Nataliya Shurupova
 */

public class LabelResource extends AbstractPscResource {
    private LabelService labelService;

    private StudyCalendarXmlCollectionSerializer<String> xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }


    public Collection<String> getAllObjects() throws ResourceException {
        String q = QueryParameters.Q.extractFrom(getRequest());
        return labelService.getFilteredLabels(q);
    }


    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            return createXmlRepresentation(getAllObjects());
        } else {
            return null;
        }
    }

    protected Representation createXmlRepresentation(Collection<String> instances) {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(instances), MediaType.TEXT_XML);
    }


    public StudyCalendarXmlCollectionSerializer<String> getXmlSerializer() {
        return xmlSerializer;
    }

    ////// CONFIGURATION

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<String> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setPlannedActivityLabelService(LabelService labelService) {
        this.labelService = labelService;
    }
}

