package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResource extends AbstractPscResource {


    private SourceDao sourceDao;

    private StudyCalendarXmlCollectionSerializer xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return new StringRepresentation(xmlSerializer.createDocumentString(sourceDao.getAll()), MediaType.TEXT_XML);

        } else {
            return null;
        }

    }


    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }


    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
}
