package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ICSRepresentation;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.MultipleAssignmentScheduleJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import edu.northwestern.bioinformatics.studycalendar.web.subject.MultipleAssignmentScheduleView;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import net.fortuna.ical4j.model.Calendar;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jalpa Patel
 * @author Rhett Sutphin
 */
public class ManagedSchedulesResource extends AbstractPscResource {
    private PscUserService pscUserService;
    private StudyCalendarXmlCollectionSerializer xmlSerializer;
    private NowFactory nowFactory;
    private TemplateService templateService;

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.GET, PscRole.STUDY_TEAM_ADMINISTRATOR,
            PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.DATA_READER);

        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));

        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setIncludeScheduledCalendar(true);
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        List<UserStudySubjectAssignmentRelationship> relatedAssignments =
            pscUserService.getManagedAssignments(getRequestedUser(), getCurrentUser());
        List<StudySubjectAssignment> assignments = extractAssignments(relatedAssignments);

        if (!assignments.isEmpty()) {
            if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
                return new StringRepresentation(
                    xmlSerializer.createDocumentString(assignments), MediaType.TEXT_XML);
            } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                return new MultipleAssignmentScheduleJsonRepresentation(
                    relatedAssignments, nowFactory, templateService, getRootRef());
            } else if (variant.getMediaType().equals(MediaType.TEXT_CALENDAR)) {
                return createICSRepresentation(relatedAssignments);
            }
        } else if (assignments.isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        }
        return null;
    }

    private List<StudySubjectAssignment> extractAssignments(
        List<UserStudySubjectAssignmentRelationship> relatedAssignments
    ) {
        List<StudySubjectAssignment> assignments
            = new ArrayList<StudySubjectAssignment>(relatedAssignments.size());
        for (UserStudySubjectAssignmentRelationship relatedAssignment : relatedAssignments) {
            assignments.add(relatedAssignment.getAssignment());
        }
        return assignments;
    }

    private PscUser getRequestedUser() throws ResourceException {
        String userName = UriTemplateParameters.USERNAME.extractFrom(getRequest());
        PscUser user = pscUserService.getAuthorizableUser(userName);
        if (user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown user: " +userName);
        }
        return user;
    }

    private Representation createICSRepresentation(List<UserStudySubjectAssignmentRelationship> assignments) throws ResourceException {
        MultipleAssignmentScheduleView schedule = new MultipleAssignmentScheduleView(assignments, nowFactory);
        Calendar icsCalendar = ICalTools.generateCalendarSkeleton();
        for (ScheduleDay scheduleDay : schedule.getDays()) {
            ICalTools.generateICSCalendarForActivities(icsCalendar, scheduleDay.getDate(),
                scheduleDay.getActivities(), getApplicationBaseUrl(), true);
        }
        return new ICSRepresentation(icsCalendar, getRequestedUser().getDisplayName());
    }

    ////// CONFIGURATION

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
