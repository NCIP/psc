package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResource extends AbstractCollectionResource<Source> {
    private ActivityService activityService;
    private SourceDao sourceDao;

    private StudyCalendarXmlCollectionSerializer<Source> xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    public Collection<Source> getAllObjects() throws ResourceException {
        String q = QueryParameters.Q.extractFrom(getRequest());
        String typeId = QueryParameters.TYPE_ID.extractFrom(getRequest());
        if (q == null && typeId == null) {
            return sourceDao.getAll();
        }
        ActivityType type = null;
        if (typeId != null) {
            try {
                type = ActivityType.getById(Integer.parseInt(typeId));
            } catch (NumberFormatException nfe) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "type-id must be an integer");
            }
        }
        return activityService.getFilteredSources(q, type, null);
    }

    public StudyCalendarXmlCollectionSerializer<Source> getXmlSerializer() {
        return xmlSerializer;
    }

    ////// CONFIGURATION

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Source> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }
}
