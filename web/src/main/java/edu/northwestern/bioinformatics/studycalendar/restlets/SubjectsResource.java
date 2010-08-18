package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Nataliya Shurupova
 */
public class SubjectsResource extends AbstractPscResource {

    private SubjectService subjectService;
    private PscUserService pscUserService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        addAuthorizationsFor(Method.GET, STUDY_SUBJECT_CALENDAR_MANAGER);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    public List <Subject> getAllObjects() throws ResourceException {
        String q = QueryParameters.Q.extractFrom(getRequest());
        return subjectService.getFilteredSubjects(q);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return createJSONRepresentation(getAllObjects());
        } else {
            return null;
        }
    }

    public Representation createJSONRepresentation(List<Subject> subjects) throws ResourceException  {
        JSONObject jsonData = new JSONObject();
        try {
            JSONArray subjectsArray = new JSONArray();
            for (Subject subject : subjects) {
                subjectsArray.put(createJSONSubject(subject));
            }
            jsonData.put("results", subjectsArray);
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
        return new JsonRepresentation(jsonData);
    }


    //TODO might want to move to helper class, if we make ScheduleRepresentationHelper more suitable for all types of objects
    public JSONObject createJSONSubject(Subject subject) throws ResourceException {
        try {
            JSONObject jsonSubject = new JSONObject();
            JSONArray jsonStudySiteInfoArray = new JSONArray();
            List<StudySubjectAssignment> studySubjectAssignments = subject.getAssignments();
            List<UserStudySubjectAssignmentRelationship> userStudySubjectAssignmentRelationships =
                    pscUserService.getVisibleAssignments(getCurrentUser());

            Set<StudySubjectAssignment> userVisibleAssignments =  new LinkedHashSet<StudySubjectAssignment>();
            for (UserStudySubjectAssignmentRelationship rel : userStudySubjectAssignmentRelationships) {
                userVisibleAssignments.add(rel.getAssignment());
            }

            Set<StudySubjectAssignment> visibleAssignments = new LinkedHashSet<StudySubjectAssignment>();
            for (StudySubjectAssignment assignment : studySubjectAssignments) {
                if (userVisibleAssignments.contains(assignment)) {
                    visibleAssignments.add(assignment);
                }
            }

            Set<StudySubjectAssignment> hiddenAssignments
                = new LinkedHashSet<StudySubjectAssignment>(studySubjectAssignments);
            for (StudySubjectAssignment visibleAssignment : visibleAssignments) {
                    hiddenAssignments.remove(visibleAssignment);
            }            

            for(StudySubjectAssignment studySubjectAssignemt : visibleAssignments) {
                JSONObject jsonStudySiteInfoObject = new JSONObject();
                Date startDate = studySubjectAssignemt.getStartDate();
                Date endDate = studySubjectAssignemt.getEndDate();
                jsonStudySiteInfoObject.put("site", studySubjectAssignemt.getStudySite().getSite().getName());
                jsonStudySiteInfoObject.put("study", studySubjectAssignemt.getStudySite().getStudy().getName());
                if (startDate != null){
                    jsonStudySiteInfoObject.put("start_date", getApiDateFormat().format(startDate));
                } else {
                    jsonStudySiteInfoObject.put("start_date", "");
                }

                if (endDate != null) {
                    jsonStudySiteInfoObject.put("end_date", getApiDateFormat().format(endDate));
                } else {
                    jsonStudySiteInfoObject.put("end_date", "");
                }
                jsonStudySiteInfoObject.put("assignment_id", studySubjectAssignemt.getGridId());
                jsonStudySiteInfoArray.put(jsonStudySiteInfoObject);
            }
            jsonSubject.put("assignments", jsonStudySiteInfoArray);

            jsonSubject.put("first_name", subject.getFirstName());
            jsonSubject.put("last_name", subject.getLastName());
            jsonSubject.put("subject_id", subject.getPersonId());
            if(subject.getDateOfBirth() != null) {
                jsonSubject.put("date_of_birth", getApiDateFormat().format(subject.getDateOfBirth()));
            } else {
                jsonSubject.put("date_of_birth", "");
            }
            jsonSubject.put("gender", subject.getGender().getDisplayName());
            jsonSubject.put("person_id", subject.getPersonId());
            if (hiddenAssignments.size()>0){
                jsonSubject.put("hidden_assignments", true);
            } else {
                jsonSubject.put("hidden_assignments", false);
            }
            jsonSubject.put("grid_id", subject.getGridId());


            return jsonSubject;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }
}


