/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
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
    public void doInit() {
        super.doInit();

        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(getRequest());
        studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest());
        assignment = studySubjectAssignmentDao.getByGridId(assignmentId);

        StudySite ss = assignment.getStudySite();

        addAuthorizationsFor(Method.GET, ss.getSite(), ss.getStudy(),
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);

        if (assignment != null) {
            scheduledCalendar = assignment.getScheduledCalendar();
        }

        String year = UriTemplateParameters.YEAR.extractFrom(getRequest());
        String month = UriTemplateParameters.MONTH.extractFrom(getRequest());
        String day = UriTemplateParameters.DAY.extractFrom(getRequest());

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