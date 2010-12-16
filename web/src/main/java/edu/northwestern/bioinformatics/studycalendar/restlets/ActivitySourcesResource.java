package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResource extends AbstractCollectionResource<Source> {
    private ActivityService activityService;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;

    private StudyCalendarXmlCollectionSerializer<Source> xmlSerializer;

    @Override
    public void doInit() {
        super.doInit();
        setAllAuthorizedFor(Method.GET);
        
        addAuthorizationsFor(Method.GET,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                BUSINESS_ADMINISTRATOR,
                DATA_READER);
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<Source> getAllObjects() throws ResourceException {
        String q = QueryParameters.Q.extractFrom(getRequest());
        String typeName = QueryParameters.TYPE.extractFrom(getRequest());
        String typeId = QueryParameters.TYPE_ID.extractFrom(getRequest());
        if (q == null && typeId == null && typeName == null) {
            return sourceDao.getAll();
        }
        ActivityType type = null;
        if (typeName != null) {
            type = activityTypeDao.getByName(typeName);
            if (type == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown activity type: " + typeName);
            }
        } else if (typeId != null) {
            try {
                type = activityTypeDao.getById(Integer.parseInt(typeId));
            } catch (NumberFormatException nfe) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "type-id must be an integer");
            }
        }
        return activityService.getFilteredSources(q, type, null);
    }

    @Override
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

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
