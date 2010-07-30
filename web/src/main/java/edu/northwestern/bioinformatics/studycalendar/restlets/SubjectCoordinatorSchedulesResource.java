package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.subject.MultipleAssignmentScheduleView;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduleRepresentationHelper;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ICSRepresentation;
import org.restlet.Context;
import org.restlet.resource.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.data.*;
import org.springframework.beans.factory.annotation.Required;
import org.acegisecurity.Authentication;

import java.util.*;
import net.fortuna.ical4j.model.Calendar;
import gov.nih.nci.cabig.ctms.lang.NowFactory;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
/**
 * @author Jalpa Patel
 */
public class SubjectCoordinatorSchedulesResource extends AbstractCollectionResource<StudySubjectAssignment> {
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer;
    private UserService userService;
    private User user;
    private NowFactory nowFactory;
    private StudySiteService studySiteService;
    private TemplateService templateService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);

        addAuthorizationsFor(Method.GET,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));
        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setIncludeScheduledCalendar(true);
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<StudySubjectAssignment> getAllObjects() throws ResourceException {
        String username = UriTemplateParameters.USERNAME.extractFrom(getRequest());
        if (username == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No username in request");
        }
        user = userService.getUserByName(username);
        if (user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown user " + username);
        }
        User currentUser = getAuthenticatedUser();
        if (currentUser.equals(user) ) {
            return user.getStudySubjectAssignments();
        } else {
            return findColleageStudySubjectAssignments(user, currentUser);
        }
    }

    public StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> getXmlSerializer() {
        return xmlSerializer;
    }

    private List<StudySubjectAssignment> findColleageStudySubjectAssignments(User user, User currentUser) {
        List<StudySite> userStudySites = studySiteService.getAllStudySitesForSubjectCoordinator(user);
        List<StudySite> currentUserStudySites = studySiteService.getAllStudySitesForSubjectCoordinator(currentUser);
        List<StudySite> colleageStudySites = new ArrayList<StudySite>();
        List<StudySubjectAssignment> colleageStudySubjectAssignments = new ArrayList<StudySubjectAssignment>();

        for (StudySite studySite: userStudySites) {
            if (currentUserStudySites.contains(studySite)) {
                colleageStudySites.add(studySite);
            }
        }

        for (StudySite studySite: colleageStudySites) {
            for (StudySubjectAssignment studySubjectAssignment: studySite.getStudySubjectAssignments()) {
                if (studySubjectAssignment.getSubjectCoordinator() != null &&
                        studySubjectAssignment.getSubjectCoordinator().equals(user)) {
                    colleageStudySubjectAssignments.add(studySubjectAssignment);
                }
            }
        }
        return colleageStudySubjectAssignments;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        List<StudySubjectAssignment> assignments = new ArrayList<StudySubjectAssignment> (getAllObjects());
        if (!assignments.isEmpty()) {
            if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
                return createXmlRepresentation(assignments);
            } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                 return new ScheduleRepresentationHelper(assignments, new ArrayList<StudySubjectAssignment>(), nowFactory, templateService );
            } else if (variant.getMediaType().equals(MediaType.TEXT_CALENDAR)) {
                return createICSRepresentation(assignments,new ArrayList<StudySubjectAssignment>());
            }
        }
        return null;
    }

    public Representation createICSRepresentation(List<StudySubjectAssignment> assignments, List<StudySubjectAssignment> hiddenAssignments) {
        MultipleAssignmentScheduleView schedule = new MultipleAssignmentScheduleView(assignments, hiddenAssignments, nowFactory);
        Calendar icsCalendar = ICalTools.generateCalendarSkeleton();
        for (ScheduleDay scheduleDay : schedule.getDays()) {
            ICalTools.generateICSCalendarForActivities(icsCalendar, scheduleDay.getDate(),
                            scheduleDay.getActivities(), getApplicationBaseUrl(), true);
        }
        return new ICSRepresentation(icsCalendar, user.getDisplayName());
    }

    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    private  User getAuthenticatedUser() {
       Authentication token = PscGuard.getCurrentAuthenticationToken(getRequest());
       return userService.getUserByName(token.getPrincipal().toString());

    }
}
