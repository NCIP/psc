package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Context;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Method;
import org.restlet.data.MediaType;
import org.springframework.beans.factory.annotation.Required;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Collection;

/**
 * TODO: this should be sensitive to the user's permissions, just like the html view.
 *
 * @author Rhett Sutphin
 */
public class StudiesResource extends AbstractPscResource {
    private StudyDao studyDao;
    private StudyCalendarXmlSerializer<Collection<Study>> xmlSerializer;

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
            return new StringRepresentation(xmlSerializer.createDocumentString(studyDao.getAll()), MediaType.TEXT_XML);
        } else {
            return null;
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlSerializer<Collection<Study>> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
}
