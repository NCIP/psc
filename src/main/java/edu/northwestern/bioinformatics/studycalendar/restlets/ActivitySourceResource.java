package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Resource representing a single {@link Source}.
 *
 * @author Rhett Sutphin
 */
public class ActivitySourceResource extends AbstractStorableDomainObjectResource<Source> {
    private SourceDao sourceDao;
    private SourceService sourceService;
    private ActivityService activityService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.STUDY_COORDINATOR);
    }

    @Override
    protected Source loadRequestedObject(Request request) {
        String sourceName = UriTemplateParameters.ACTIVITY_SOURCE_NAME.extractFrom(request);
        Source source = sourceDao.getByName(sourceName);
        if (source == null) return null;

        String q = QueryParameters.Q.extractFrom(request);
        String typeId = QueryParameters.TYPE_ID.extractFrom(request);
        if (q == null && typeId == null) return source;

        ActivityType type = null;
        if (typeId != null) {
            try {
                type = ActivityType.getById(Integer.parseInt(typeId));
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
    public void store(Source source) {
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
}