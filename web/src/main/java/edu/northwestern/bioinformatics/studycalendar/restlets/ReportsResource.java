package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ReportJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ReportCsvRepresentation;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import org.restlet.data.*;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.restlet.Context;
import org.springframework.beans.factory.annotation.Required;
import java.util.*;
import java.util.Date;
import java.text.ParseException;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Nataliya Shurupova
 */
public class ReportsResource extends AbstractCollectionResource<ScheduledActivitiesReportRow> {
    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private AuthorizationService authorizationService;
    private StudyDao studyDao;


    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR, Role.SITE_COORDINATOR);

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
        ScheduledActivitiesReportFilters filters = getFilters();
        List<ScheduledActivitiesReportRow> scheduledActivitiesReportRow = scheduledActivitiesReportRowDao.search(filters);

        return scheduledActivitiesReportRow;
    }

    public List<ScheduledActivitiesReportRow> filteredRows(List<ScheduledActivitiesReportRow> rows) {
        List<ScheduledActivitiesReportRow> filteredRows = new ArrayList<ScheduledActivitiesReportRow>();
        User user = getLegacyCurrentUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR));
        List<StudySite> ownedStudySites = authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR));
        for (ScheduledActivitiesReportRow row : rows) {
            for (StudySite studySite : ownedStudySites) {
                if (row.getStudy().getId().equals(studySite.getStudy().getId()) && row.getSite().getId().equals(studySite.getSite().getId())) {
                    filteredRows.add(row);
                }
            }
        }
        return filteredRows;
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
        List<ScheduledActivitiesReportRow> filteredRows = filteredRows(allRows);
        boolean messageIndicator = false;
        if (allRows.size() > filteredRows.size()) {
            messageIndicator = true;
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return new ReportJsonRepresentation(getFilters(), filteredRows, messageIndicator);
        }
        if (variant.getMediaType().equals(PscMetadataService.TEXT_CSV)) {
            return new ReportCsvRepresentation(filteredRows, ',');
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_EXCEL)) {
            return new ReportCsvRepresentation(filteredRows, '\t');
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

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
