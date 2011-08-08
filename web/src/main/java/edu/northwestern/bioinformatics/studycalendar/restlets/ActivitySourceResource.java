package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ActivitySourcesJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;

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
    private int total;
    private Integer offset;
    private Integer limit;

    public ActivitySourceResource() {
    }

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
        Integer limit = extractLimit();
        int total = source.getActivities().size();
        Integer offset = extractOffset(total);
        if (q == null && typeId == null && typeName == null && limit== null && offset == null) return source;
        setLimit(limit);
        setTotal(total);
        setOffset(offset);
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

        List<Source> filtered = activityService.getFilteredSources(q, type, source, limit, offset);

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
    public Source store(Source source) throws ResourceException {
        if (getRequestedObject() == null) {
            sourceDao.save(source);
        } else {
            Source existingSource = getRequestedObject();
            sourceService.updateSource(source,existingSource);
        }
        return source;
    }

    private Integer extractLimit() throws ResourceException {
        String limitS = QueryParameters.LIMIT.extractFrom(getRequest());
        if (limitS == null) return null;
        try {
            Integer limit = new Integer(limitS);
            if (limit < 1) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
            }
            return limit;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
        }
    }

    private Integer extractOffset(int total) throws ResourceException {
        String offsetS = QueryParameters.OFFSET.extractFrom(getRequest());
        if (offsetS == null) return 0;
        try {
            Integer offset = new Integer(offsetS);
            if (offset < 0) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
            }
            if (offset >= total && offset > 0) {
                throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, String.format(
                    "Offset %d is too large.  There are %d result(s), so the max offset is %d.",
                    offset, total, Math.max(0, total - 1)));
            }
            return offset;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
        }
    }

    @Override
    protected Representation createJsonRepresentation(Source object) {
        return new ActivitySourcesJsonRepresentation(object.getActivities(), getTotal(), getOffset(), getLimit(), activityTypeDao.getAll());
    }

    // Getters
    public int getTotal() {
        return total;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }

    // Setters
    public void setTotal(int total) {
        this.total = total;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    // Configuration
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