package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ReportCsvRepresentation;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ReportJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.ReportService;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Nataliya Shurupova
 */
public class ReportsResource extends AbstractCollectionResource<ScheduledActivitiesReportRow> {
    private ActivityTypeDao activityTypeDao;
    private ReportService reportService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        addAuthorizationsFor(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(PscMetadataService.TEXT_CSV));
        getVariants().add(new Variant(MediaType.APPLICATION_EXCEL));
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<ScheduledActivitiesReportRow> getAllObjects() throws ResourceException {
        return reportService.searchScheduledActivities(getFilters());
    }

    // exposed for testing
    ScheduledActivitiesReportFilters getFilters() throws ResourceException {
        String study = FilterParameters.STUDY.extractFrom(getRequest());
        String site = FilterParameters.SITE.extractFrom(getRequest());
        String state = FilterParameters.STATE.extractFrom(getRequest());
        String activity_type = FilterParameters.ACTIVITY_TYPE.extractFrom(getRequest());               
        String label = FilterParameters.LABEL.extractFrom(getRequest());
        String start_date = FilterParameters.START_DATE.extractFrom(getRequest());
        String end_date = FilterParameters.END_DATE.extractFrom(getRequest());
        String responsible_user = FilterParameters.RESPONSIBLE_USER.extractFrom(getRequest());
        String person_id = FilterParameters.PERSON_ID.extractFrom(getRequest());

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
            // TODO: #1111
            throw new StudyCalendarError("TODO: issue #1111");
        }

        filters.setSiteName(site);
        filters.setLabel(label);
        filters.setStudyAssignedIdentifier(study);
        filters.setPersonId(person_id);

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
            return new ReportJsonRepresentation(getFilters(), allRows, 0);
        }
        if (variant.getMediaType().equals(PscMetadataService.TEXT_CSV)) {
            return new ReportCsvRepresentation(allRows, ',');
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_EXCEL)) {
            return new ReportCsvRepresentation(allRows, '\t');
        }
        return null;
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<ScheduledActivitiesReportRow> getXmlSerializer() {
        return null;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }
}
