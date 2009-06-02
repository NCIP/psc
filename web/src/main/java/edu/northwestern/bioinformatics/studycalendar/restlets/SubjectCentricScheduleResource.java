package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.data.*;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Required;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.*;
import java.text.SimpleDateFormat;


/**
 * @author Jalpa Patel
 */
public class SubjectCentricScheduleResource extends AbstractCollectionResource<StudySubjectAssignment> {
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer;
    private SubjectDao subjectDao;
    private AuthorizationService authorizationService;
    private NowFactory nowFactory;

    private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setSubjectCentric(true);
        
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<StudySubjectAssignment> getAllObjects() throws ResourceException {
        String subjectId = UriTemplateParameters.SUBJECT_IDENTIFIER.extractFrom(getRequest());
        if (subjectId == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No subject identifier in request");
        }
        Subject subject = subjectDao.findSubjectByPersonId(subjectId);
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
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(visibleAssignments);
        }
        else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return createJSONRepresentation(allAssignments,visibleAssignments);
        }
        return null;
    }

    public Representation createJSONRepresentation(List<StudySubjectAssignment> allAssignments,List<StudySubjectAssignment> visibleAssignments)
            throws ResourceException  {
        Set<StudySubjectAssignment> hiddenAssignments
                = new LinkedHashSet<StudySubjectAssignment>(allAssignments);
        for (StudySubjectAssignment visibleAssignment : visibleAssignments) {
                hiddenAssignments.remove(visibleAssignment);
        }
        SubjectCentricSchedule schedule = new SubjectCentricSchedule(
            visibleAssignments, new ArrayList<StudySubjectAssignment>(hiddenAssignments), nowFactory);
        JSONObject jsonData = new JSONObject();
        try {
            JSONObject dayWiseActivities = new JSONObject();
            for (ScheduleDay scheduleDay : schedule.getDays()) {
                if (!scheduleDay.getActivities().isEmpty()) {
                    dayWiseActivities.put(new String(dayFormatter.format(scheduleDay.getDate())),
                           ScheduleRepresentationHelper.createJSONScheduledActivities(scheduleDay.getHasHiddenActivities(), scheduleDay.getActivities()));
                }
            }
            JSONArray studySegments = new JSONArray();
            for (StudySubjectAssignment studySubjectAssignment: visibleAssignments) {
                for (ScheduledStudySegment scheduledStudySegment : studySubjectAssignment.getScheduledCalendar().getScheduledStudySegments()) {
                    studySegments.put(ScheduleRepresentationHelper.createJSONStudySegment(scheduledStudySegment));
                }
            }
            jsonData.put("days", dayWiseActivities);
            jsonData.put("study_segments", studySegments);
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
        return new JsonRepresentation(jsonData);
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
