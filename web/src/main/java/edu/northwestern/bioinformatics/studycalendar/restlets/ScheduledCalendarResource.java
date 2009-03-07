package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.NextScheduledStudySegmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * @author John Dzak
 */
public class ScheduledCalendarResource extends AbstractDomainObjectResource<ScheduledCalendar> {
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private StudyDao studyDao;
    private ScheduledStudySegmentXmlSerializer scheduledStudySegmentSerializer;
    private NextScheduledStudySegmentXmlSerializer nextScheduledStudySegmentSerializer;
    private SubjectService subjectService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);

    }


    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    protected ScheduledCalendar loadRequestedObject(Request request) {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        Study study = studyDao.getByAssignedIdentifier(studyIdent);
        if (study == null) return null;

        String assignmentId = findAssignmentId(request);
        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentId);
        if (assignment == null) {
            assignment = studySubjectAssignmentDao.getByStudySubjectIdentifier(study, assignmentId);
        }
        if (assignment != null) {
            return assignment.getScheduledCalendar();
        }
        return null;
    }

    private boolean isICSRequest(Request request) {
        return StringUtils.contains(request.getResourceRef().getPath(), ".ics");
    }

    /**
     * Finds grid id in request.
     *
     * @param request the request
     */
    private String findAssignmentId(final Request request) {
        // the grid Id should be in following form "/%s/studies/%s/schedules/[assignment-grid-id].ics"

        String pathInfo = request.getResourceRef().getPath();
        if (!isICSRequest(request)) {
            return UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        }
        int beginIndex = pathInfo.indexOf("schedules/");

        int endIndex = pathInfo.indexOf(".ics");

        return pathInfo.substring(beginIndex + 10, endIndex);
    }

    @Override
    protected Representation createCalendarRepresentation(ScheduledCalendar scheduledCalendar) {
        StudySubjectAssignment studySubjectAssignment = scheduledCalendar.getAssignment();
        Representation representation = new ICSRepresentation(studySubjectAssignment);
        return representation;
    }

    public void acceptRepresentation(final Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {

            NextScheduledStudySegment scheduled;
            try {
                scheduled = nextScheduledStudySegmentSerializer.readDocument(entity.getStream());
            } catch (IOException e) {
                log.warn("PUT failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());
            }

            ScheduledStudySegment scheduledSegment = store(scheduled);

            getResponse().setEntity(
                    new StringRepresentation(
                            scheduledStudySegmentSerializer.createDocumentString(scheduledSegment), MediaType.TEXT_XML));

            getResponse().setStatus(Status.SUCCESS_CREATED);

        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
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
}
