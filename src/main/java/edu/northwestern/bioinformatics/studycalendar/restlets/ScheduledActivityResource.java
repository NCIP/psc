package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivityResource extends AbstractCollectionResource<ScheduledActivity> {


    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private Date date;
    private ScheduledCalendar scheduledCalendar;

    private StudyCalendarXmlCollectionSerializer scheduledActivityXmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);

        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentId);
        if (assignment != null) {
            scheduledCalendar = assignment.getScheduledCalendar();


        }
        String year = UriTemplateParameters.YEAR.extractFrom(request);

        String month = UriTemplateParameters.MONTH.extractFrom(request);

        String day = UriTemplateParameters.DAY.extractFrom(request);

        //  month starts with 0
        try {
            date = DateUtils.createDate(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        } catch (Exception e) {
            log.error("Could not parse the date.");
        }


    }

    @Override
    public Collection<ScheduledActivity> getAllObjects() {
        if (scheduledCalendar == null || date == null) {
            String message = "either scheduled calendar is null or date is null.scheduled calendar:" + scheduledCalendar + "; date:" + date;
            log.error(message);
            return new ArrayList<ScheduledActivity>();
        }
        Collection<ScheduledActivity> scheduledActivityCollection = scheduledActivityDao.getEventsByDate(scheduledCalendar, date, date);
        return scheduledActivityCollection;
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