package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ReportRepresentation;
import org.restlet.data.*;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.restlet.Context;
import org.springframework.beans.factory.annotation.Required;
import java.util.*;
import java.util.Date;
import java.text.ParseException;

/**
 * @author Nataliya Shurupova
 */
public class ReportsResource extends AbstractCollectionResource<ScheduledActivitiesReportRow> {
    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<ScheduledActivitiesReportRow> getAllObjects() throws ResourceException {
        ScheduledActivitiesReportFilters filters = getFilters();
        List<ScheduledActivitiesReportRow> scheduledActivitiesReportRow = scheduledActivitiesReportRowDao.search(filters);
        return scheduledActivitiesReportRow;
    }

    public ScheduledActivitiesReportFilters getFilters() throws ResourceException {
        String study = FilterParameters.STUDY.extractFrom(getRequest());
        String site = FilterParameters.SITE.extractFrom(getRequest());
        String state = FilterParameters.STATE.extractFrom(getRequest());
        String activity_type = FilterParameters.ACTIVITY_TYPE.extractFrom(getRequest());
        String label = FilterParameters.LABEL.extractFrom(getRequest());
        String start_date = FilterParameters.START_DATE.extractFrom(getRequest());
        String end_date = FilterParameters.END_DATE.extractFrom(getRequest());
        String responsible_user = FilterParameters.RESPONSIBLE_USER.extractFrom(getRequest());

        ScheduledActivitiesReportFilters filters = new ScheduledActivitiesReportFilters();
        if (activity_type != null) {
            ActivityType activityType = activityTypeDao.getById(new Integer(activity_type));
            filters.setActivityType(activityType);
        }

        if (state != null) {
            ScheduledActivityMode scheduledActivityMode = ScheduledActivityMode.getById(new Integer(state));
            filters.setCurrentStateMode(scheduledActivityMode);
        }

        if (responsible_user != null) {
            User user = userDao.getById(new Integer(responsible_user));
            filters.setSubjectCoordinator(user);
        }

        filters.setSiteName(site);
        filters.setLabel(label);
        filters.setStudyAssignedIdentifier(study);

        MutableRange<Date> range = new MutableRange<Date>();
        if (start_date != null) {
            try {
                Date startDate = getApiDateFormat().parse(start_date);
                range.setStart(startDate);
            } catch (ParseException pe) {
              throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", pe);
            }
        }
        if (end_date != null) {
            try {
                Date endDate = getApiDateFormat().parse(end_date);
                range.setStop(endDate);
                filters.setActualActivityDate(range);
            } catch (ParseException pe) {
              throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", pe);
            }
        }

        filters.setActualActivityDate(range);
        return filters;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        List<ScheduledActivitiesReportRow> allRows = new ArrayList<ScheduledActivitiesReportRow>(getAllObjects());
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return new ReportRepresentation(getFilters(), allRows);
        }
        return null;
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<ScheduledActivitiesReportRow> getXmlSerializer() {
        return null;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }     

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    @Required
    public void setScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao) {
        this.scheduledActivitiesReportRowDao = scheduledActivitiesReportRowDao;
    }
}
