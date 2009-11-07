package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduleRepresentationHelper;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ICSRepresentation;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;

/**
 * @author Jalpa Patel
 */
public class SubjectCentricScheduleResource extends AbstractCollectionResource<StudySubjectAssignment> {
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer;
    private SubjectDao subjectDao;
    private AuthorizationService authorizationService;
    private NowFactory nowFactory;
    private Subject subject;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));
        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setSubjectCentric(true);
        
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<StudySubjectAssignment> getAllObjects() throws ResourceException {
        String subjectId = UriTemplateParameters.SUBJECT_IDENTIFIER.extractFrom(getRequest());
        if (subjectId == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No subject identifier in request");
        }
        subject = subjectDao.findSubjectByPersonId(subjectId);
        if (subject == null) {
            subject = subjectDao.getByGridId(subjectId);
            if (subject == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"Subject doesn't exist with id "+subjectId);
            }
        }
        return subject.getAssignments();
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        List<StudySubjectAssignment> allAssignments = new ArrayList<StudySubjectAssignment> (getAllObjects());
        List<StudySubjectAssignment> visibleAssignments
                = authorizationService.filterAssignmentsForVisibility(allAssignments, getCurrentUser());
        if (visibleAssignments.isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "User " +getCurrentUser().getDisplayName()+" doesn't have permission to access schedule of " +subject.getFullName() );
        }
        Set<StudySubjectAssignment> hiddenAssignments
                = new LinkedHashSet<StudySubjectAssignment>(allAssignments);
        for (StudySubjectAssignment visibleAssignment : visibleAssignments) {
                hiddenAssignments.remove(visibleAssignment);
        }
        SubjectCentricSchedule schedule = new SubjectCentricSchedule(
            visibleAssignments, new ArrayList<StudySubjectAssignment>(hiddenAssignments), nowFactory);
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(visibleAssignments);
        }
        else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return ScheduleRepresentationHelper.createJSONRepresentation(schedule, visibleAssignments);
        }
        else if (variant.getMediaType().equals(MediaType.TEXT_CALENDAR)) {
            return  createICSRepresentation(schedule);
        }
        return null;
    }

    public Representation createICSRepresentation(SubjectCentricSchedule schedule) {
        Calendar icsCalendar = ICalTools.generateCalendarSkeleton();
        for (ScheduleDay scheduleDay : schedule.getDays()) {
            ICalTools.generateICSCalendarForActivities(icsCalendar, scheduleDay.getDate(), scheduleDay.getActivities(), getApplicationBaseUrl(), false);
        }
        return new ICSRepresentation(icsCalendar, subject.getFullName());
    }

    public StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }
}
