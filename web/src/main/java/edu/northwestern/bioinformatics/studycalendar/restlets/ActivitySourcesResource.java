package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ActivitySourcesJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResource extends AbstractCollectionResource<Source> {
    private ActivityService activityService;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;
    private ActivityDao activityDao;

    private StudyCalendarXmlCollectionSerializer<Source> xmlSerializer;

    @Override
    public void doInit() {
        super.doInit();
        setAllAuthorizedFor(Method.GET);

        addAuthorizationsFor(Method.GET,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                BUSINESS_ADMINISTRATOR,
                DATA_READER);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    public Collection<Source> getAllObjects() throws ResourceException {
        throw new UnsupportedOperationException("This method is not used.");
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        ActivitySearchParameters params = new ActivitySearchParameters(getRequest(), activityDao.getCount(), activityTypeDao);
        List<ActivityType> types = activityTypeDao.getAll();

        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            if (params.isAllBlank()) {
                return new ActivitySourcesJsonRepresentation(activityDao.getAll(), params.getTotal(), null, null, types);
            }
            List<Activity> matches = activityDao.getActivitiesBySearchText(
                    params.getQ(),
                    params.getType(),
                    null,
                    params.getLimit(),
                    params.getOffset(),
                    ActivityDao.ActivitySearchCriteria.findCriteria(params.getSort()),
                    params.getOrder()
            );
            return new ActivitySourcesJsonRepresentation(matches, params.getTotal(), params.getOffset(), params.getLimit(), types);
        } else if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            if (params.isAllBlank()) {
                return createXmlRepresentation(sourceDao.getAll());
            }
            List<Source> sources = activityService.getFilteredSources(
                    params.getQ(),
                    params.getType(),
                    null,
                    params.getLimit(),
                    params.getOffset(),
                    ActivityDao.ActivitySearchCriteria.findCriteria(params.getSort()),
                    params.getOrder()
            );
            return createXmlRepresentation(sources);
        } else {
            return null;
        }
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
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
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
