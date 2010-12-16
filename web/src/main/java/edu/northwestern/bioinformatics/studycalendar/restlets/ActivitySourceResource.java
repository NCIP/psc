package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * Resource representing a single {@link Source}.
 *
 * @author Rhett Sutphin
 */
public class ActivitySourceResource extends AbstractStorableDomainObjectResource<Source> {
    private SourceDao sourceDao;
    private SourceService sourceService;
    private ActivityService activityService;
    private ActivityTypeDao activityTypeDao;

    @Override
    public void doInit() {
        super.doInit();

        addAuthorizationsFor(Method.GET,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                BUSINESS_ADMINISTRATOR,
                DATA_READER);

        addAuthorizationsFor(Method.PUT, BUSINESS_ADMINISTRATOR);
    }

    @Override
    protected Source loadRequestedObject(Request request) {
        String sourceName = UriTemplateParameters.ACTIVITY_SOURCE_NAME.extractFrom(request);
        Source source = sourceDao.getByName(sourceName);
        if (source == null) return null;

        String q = QueryParameters.Q.extractFrom(request);
        String typeName = QueryParameters.TYPE.extractFrom(getRequest());
        String typeId = QueryParameters.TYPE_ID.extractFrom(request);
        if (q == null && typeId == null && typeName == null) return source;

        ActivityType type = null;
        if (typeName != null) {
            type = activityTypeDao.getByName(typeName);
            if (type == null) {
                setClientErrorReason("Unknown activity type: " + typeName);
                return null;
            }
        } else if (typeId != null) {
            try {
                type = activityTypeDao.getById(Integer.parseInt(typeId));
            } catch (NumberFormatException nfe) {
                setClientErrorReason("type-id must be an integer");
                return null;
            }
        }

        List<Source> filtered = activityService.getFilteredSources(q, type, source);

        if (filtered.size() == 0) {
            Source empty = new Source();
            empty.setName(source.getName());
            empty.setMemoryOnly(true);
            return empty;
        } else {
            return filtered.get(0);
        }
    }

    @Override
    public void store(Source source) throws ResourceException {
        if (getRequestedObject() == null) {
            sourceDao.save(source);
        } else {
            Source existingSource = getRequestedObject();
            sourceService.updateSource(source,existingSource);
        }
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
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