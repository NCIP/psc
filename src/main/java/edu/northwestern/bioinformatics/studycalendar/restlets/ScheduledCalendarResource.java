package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
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
    private ScheduledStudySegmentXmlSerializer scheduledSegmentSerializer;
    private ScheduledCalendarDao scheduledCalendarDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    @Override
    public boolean allowPost() { return true; }

    @Override
    protected ScheduledCalendar loadRequestedObject(Request request) {
        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentId);
        if (assignment != null) {
            return assignment.getScheduledCalendar();
        }
        return null;
    }


    public void acceptRepresentation(final Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {

            ScheduledStudySegment segment;
            try {
                segment = scheduledSegmentSerializer.readDocument(entity.getStream());
                store(segment);
            } catch (IOException e) {
                log.warn("PUT failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());
            }

            getResponse().setEntity(
                    new StringRepresentation(
                            scheduledSegmentSerializer.createDocumentString(segment), MediaType.TEXT_XML));

            getResponse().setStatus(Status.SUCCESS_CREATED);

        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    private void store(ScheduledStudySegment segment) {
        ScheduledCalendar cal = getRequestedObject();
        cal.addStudySegment(segment);
        scheduledCalendarDao.save(cal);
    }

    ////// Bean setters
    
    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setScheduledStudySegmentXmlSerializer(ScheduledStudySegmentXmlSerializer scheduledSegmentSerializer) {
        this.scheduledSegmentSerializer = scheduledSegmentSerializer;
    }

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }
}
