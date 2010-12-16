package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduledActivityReportCsvRepresentation;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduledActivityReportJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.ReportService;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.QueryParameters.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static java.lang.String.format;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
public class ScheduledActivityReportResource extends AbstractCollectionResource<ScheduledActivitiesReportRow> {
    private ActivityTypeDao activityTypeDao;
    private ReportService reportService;
    private AuthorizationManager csmAuthorizationManager;

    @Override
    public void doInit() {
        super.doInit();

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
        return reportService.searchScheduledActivities(buildFilters());
    }

    // exposed for testing
    ScheduledActivitiesReportFilters buildFilters() throws ResourceException {
        ScheduledActivitiesReportFilters filters = new ScheduledActivitiesReportFilters();

        applyActivityTypeFilter(filters);
        applyStateFilter(filters);
        applyResponsibleUserFilter(filters);
        applyDateRangeFilters(filters);

        filters.setSiteName(SITE.extractFrom(getRequest()));
        filters.setLabel(LABEL.extractFrom(getRequest()));
        filters.setStudyAssignedIdentifier(STUDY.extractFrom(getRequest()));
        filters.setPersonId(PERSON_ID.extractFrom(getRequest()));

        return filters;
    }

    private void applyDateRangeFilters(ScheduledActivitiesReportFilters filters) throws ResourceException {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(parseDateFilter(START_DATE));
        range.setStop(parseDateFilter(END_DATE));
        filters.setActualActivityDate(range);
    }

    private Date parseDateFilter(QueryParameters dateParam) throws ResourceException {
        String dateString = dateParam.extractFrom(getRequest());
        if (dateString == null) return null;
        try {
            return getApiDateFormat().parse(dateString);
        } catch (ParseException pe) {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                format("Unparseable value for %s filter: %s.  Expected format is %s.",
                    dateParam.attributeName(), dateString, API_DATE_FORMAT_STRING));
        }
    }

    private void applyResponsibleUserFilter(ScheduledActivitiesReportFilters filters) throws ResourceException {
        String responsible_user = RESPONSIBLE_USER.extractFrom(getRequest());
        if (responsible_user != null) {
            User csmUser = csmAuthorizationManager.getUser(responsible_user);
            if (csmUser == null) {
                throw new ResourceException(
                    Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                    format("Unknown user for %s filter: %s",
                        RESPONSIBLE_USER.attributeName(), responsible_user));
            } else {
                filters.setResponsibleUser(csmUser);
            }
        }
    }

    private void applyStateFilter(
        ScheduledActivitiesReportFilters filters
    ) throws ResourceException {
        Collection<String> states = STATE.extractAllFrom(getRequest());
        if (!states.isEmpty()) {
            List<ScheduledActivityMode> modes = new ArrayList<ScheduledActivityMode>(states.size());
            for (String state : states) {
                ScheduledActivityMode mode = ScheduledActivityMode.getByName(state);
                if (mode != null) {
                    modes.add(mode);
                } else {
                    throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                        "Invalid scheduled activity state name for state filter: " + state);
                }
            }
            filters.setCurrentStateModes(modes);
        }
    }

    private void applyActivityTypeFilter(
        ScheduledActivitiesReportFilters filters
    ) throws ResourceException {
        Collection<String> typeNames = ACTIVITY_TYPE.extractAllFrom(getRequest());
        if (!typeNames.isEmpty()) {
            List<ActivityType> activityTypes = new ArrayList<ActivityType>(typeNames.size());
            for (String typeName : typeNames) {
                ActivityType activityType = activityTypeDao.getByNameIgnoringCase(typeName);
                if (activityType != null) {
                    activityTypes.add(activityType);
                } else {
                    throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                        format("Unknown activity type for activity-type filter: %s", typeName));
                }
            }
            filters.setActivityTypes(activityTypes);
        }
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        List<ScheduledActivitiesReportRow> allRows = new ArrayList<ScheduledActivitiesReportRow>(getAllObjects());
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return new ScheduledActivityReportJsonRepresentation(buildFilters(), allRows);
        }
        if (variant.getMediaType().equals(PscMetadataService.TEXT_CSV)) {
            return new ScheduledActivityReportCsvRepresentation(allRows, ',');
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_EXCEL)) {
            return new ScheduledActivityReportCsvRepresentation(allRows, '\t');
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

    @Required
    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }
}
