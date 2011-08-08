package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
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
    private ActivityDao activityDao;

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

        ActivitySearchParameters params = new ActivitySearchParameters(getRequest(), source.getActivities().size(), activityTypeDao);

        if (params.isAllBlank()) return source;

        setLimit(params.getLimit());
        setTotal(params.getTotal());
        setOffset(params.getOffset());

        List<Source> filtered = activityService.getFilteredSources(
                params.getQ(),
                params.getType(),
                source,
                params.getLimit(),
                params.getOffset(),
                ActivityDao.ActivitySearchCriteria.findCriteria(params.getSort()),
                params.getOrder()
        );

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