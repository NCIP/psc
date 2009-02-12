package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Date;

/**
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
public class ScheduledActivitiesResource extends AbstractCollectionResource<ScheduledActivity> {


    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private Date date;
    private ScheduledCalendar scheduledCalendar;
    private String studyIdentifier;

    private StudyCalendarXmlCollectionSerializer scheduledActivityXmlSerializer;
    private StudySubjectAssignment assignment;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);

        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);

        assignment = studySubjectAssignmentDao.getByGridId(assignmentId);

        if (assignment != null) {
            scheduledCalendar = assignment.getScheduledCalendar();
        }

        String year = UriTemplateParameters.YEAR.extractFrom(request);
        String month = UriTemplateParameters.MONTH.extractFrom(request);
        String day = UriTemplateParameters.DAY.extractFrom(request);

        try {
            date = DateTools.createDate(
                Integer.parseInt(year),
                Integer.parseInt(month) - 1, // The Calendar month constants start with 0 
                Integer.parseInt(day));
        } catch (NumberFormatException e) {
            log.error("Could not parse the date due to an invalid number in the URL", e);
        }
    }

    @Override
    public Collection<ScheduledActivity> getAllObjects() throws ResourceException {
        if (date == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse date from URI");
        } else if (!assignment.getStudySite().getStudy().getAssignedIdentifier().equals(studyIdentifier)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, String.format(
                "The designated schedule (%s) is not related to the designated study (%s)",
                assignment.getGridId(), studyIdentifier));
        }
        return scheduledActivityDao.getEventsByDate(assignment.getScheduledCalendar(), date, date);
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<ScheduledActivity> getXmlSerializer() {
        return scheduledActivityXmlSerializer;
    }

    @Required
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setScheduledActivityDao(final ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setScheduledActivityXmlSerializer(final StudyCalendarXmlCollectionSerializer scheduledActivityXmlSerializer) {
        this.scheduledActivityXmlSerializer = scheduledActivityXmlSerializer;
    }
}