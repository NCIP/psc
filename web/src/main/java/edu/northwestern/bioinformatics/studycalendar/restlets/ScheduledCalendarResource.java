package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ICSRepresentation;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.NextScheduledStudySegmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import net.fortuna.ical4j.model.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledCalendarResource extends AbstractDomainObjectResource<ScheduledCalendar> {
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private StudyDao studyDao;
    private ScheduledStudySegmentXmlSerializer scheduledStudySegmentSerializer;
    private NextScheduledStudySegmentXmlSerializer nextScheduledStudySegmentSerializer;
    private SubjectService subjectService;
    private TemplateService templateService;
    private Study study;
    private ScheduleService scheduleService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
        getVariants().add(new Variant(MediaType.TEXT_CALENDAR));
    }


    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    protected ScheduledCalendar loadRequestedObject(Request request) throws ResourceException {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        study = studyDao.getByAssignedIdentifier(studyIdent);
        
        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Study " +studyIdent +" is not found");
        }

        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentId);
        if (assignment == null) {
            assignment = studySubjectAssignmentDao.getByStudySubjectIdentifier(study, assignmentId);
        }
        if (assignment != null) {
            return assignment.getScheduledCalendar();
        }
        return null;
    }

    @Override
    protected Representation createCalendarRepresentation(ScheduledCalendar scheduledCalendar) {
        Calendar icsCalendar = ICalTools.generateCalendarSkeleton();
        StudySubjectAssignment studySubjectAssignment = scheduledCalendar.getAssignment();

        for (ScheduledStudySegment scheduledStudySegment : studySubjectAssignment.getScheduledCalendar().getScheduledStudySegments()) {
            SortedMap<Date, List<ScheduledActivity>> events = scheduledStudySegment.getActivitiesByDate();
                for (Date date : events.keySet()) {
                    ICalTools.generateICSCalendarForActivities(icsCalendar, date, events.get(date), getApplicationBaseUrl(), false);
                }
        }
        return new ICSRepresentation(icsCalendar, studySubjectAssignment.getName());
    }

    public void acceptRepresentation(final Representation entity) throws ResourceException {
        if (entity.getMediaType().equals(MediaType.TEXT_XML)) {

            NextScheduledStudySegment scheduled;
            try {
                scheduled = nextScheduledStudySegmentSerializer.readDocument(entity.getStream());
                scheduleService.resolveNextScheduledStudySegment(scheduled);
            } catch (IOException e) {
                log.warn("PUT failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());
            }

            if (!study.equals(templateService.findStudy(scheduled.getStudySegment()))) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "StudySegment with identifier " + scheduled.getStudySegment().getGridId()+" is not part of study " +study.getNaturalKey());
            }

            ScheduledStudySegment scheduledSegment = store(scheduled);

            getResponse().setEntity(
                    new StringRepresentation(
                            scheduledStudySegmentSerializer.createDocumentString(scheduledSegment), MediaType.TEXT_XML));

            getResponse().setStatus(Status.SUCCESS_CREATED);

        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + entity.getMediaType());
        }
    }

    private ScheduledStudySegment store(NextScheduledStudySegment scheduled) throws ResourceException {
        ScheduledCalendar cal = getRequestedObject();
        return subjectService.scheduleStudySegment(cal.getAssignment(), scheduled.getStudySegment(), scheduled.getStartDate(), scheduled.getMode());
    }

    ////// Bean setters

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setScheduledStudySegmentXmlSerializer(ScheduledStudySegmentXmlSerializer scheduledSegmentSerializer) {
        this.scheduledStudySegmentSerializer = scheduledSegmentSerializer;
    }

    @Required
    public void setNextScheduledStudySegmentXmlSerializer(NextScheduledStudySegmentXmlSerializer nextScheduledStudySegmentSerializer) {
        this.nextScheduledStudySegmentSerializer = nextScheduledStudySegmentSerializer;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
}
