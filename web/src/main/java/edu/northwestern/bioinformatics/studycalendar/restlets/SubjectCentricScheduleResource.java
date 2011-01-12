package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ICSRepresentation;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduleRepresentationHelper;
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
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Jalpa Patel
 */
public class SubjectCentricScheduleResource extends AbstractCollectionResource<StudySubjectAssignment> {
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer;
    private SubjectDao subjectDao;
    private NowFactory nowFactory;
    private Subject subject;
    private TemplateService templateService;

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));
        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setSubjectCentric(true);
    }

    @Override
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
    public Representation get(Variant variant) throws ResourceException {
        Collection<StudySubjectAssignment> all = getAllObjects();

        List<UserStudySubjectAssignmentRelationship> related =
            new ArrayList<UserStudySubjectAssignmentRelationship>(all.size());

        List<StudySubjectAssignment> visible =
            new ArrayList<StudySubjectAssignment>(related.size());
        
        for (StudySubjectAssignment a : all) {
            UserStudySubjectAssignmentRelationship rel = new UserStudySubjectAssignmentRelationship(getCurrentUser(), a);
            related.add(rel);
            if (rel.isVisible()) visible.add(a);
        }

        if (visible.isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
                "User " + getCurrentUser() + " doesn't have permission to view this schedule");
        }
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(visible);
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return new ScheduleRepresentationHelper(related, nowFactory, templateService, subject);
        } else if (variant.getMediaType().equals(MediaType.TEXT_CALENDAR)) {
            return  createICSRepresentation(related);
        }
        return null;
    }

    public Representation createICSRepresentation(List<UserStudySubjectAssignmentRelationship> relatedAssignments) {
        MultipleAssignmentScheduleView schedule = new MultipleAssignmentScheduleView(
            relatedAssignments, nowFactory);
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
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
