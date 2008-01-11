package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.ActivityXMLReader;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceResource extends Resource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ActivityXMLReader activityXMLReader;
    private SourceDao sourceDao;

    private Source requestedSource;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        getVariants().add(new Variant(MediaType.TEXT_XML));

        String sourceName = Reference.decode((String) request.getAttributes().get(UriTemplateParameters.SOURCE_NAME.attributeName()));
        requestedSource = sourceDao.getByName(sourceName);
        if (requestedSource == null) {
            log.debug("Requested source {} not present", sourceName);
            setAvailable(false);
        }
        else {
            log.debug("Requested source {} is {}", sourceName, requestedSource);
        }
    }

    @Override public boolean allowPut()    { return true; }
    @Override public boolean allowPost()   { return false; }
    @Override public boolean allowDelete() { return false; }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (requestedSource == null) return null;

        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(requestedSource);
        } else {
            return null;
        }
    }

    private Representation createXmlRepresentation(Source source) {
//        return new StringRepresentation(new ActivityXMLWriter().createStudyXML(study), MediaType.TEXT_XML);
        throw new UnsupportedOperationException("TODO");
    }

    ////// CONFIGURATION

    @Required
    public void setActivityXMLReader(ActivityXMLReader activityXMLReader) {
        this.activityXMLReader = activityXMLReader;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
}
