package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.*;
import org.restlet.data.*;
import org.springframework.beans.factory.annotation.Required;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.*;
import java.text.SimpleDateFormat;


/**
 * @author Nataliya Shurupova
 */
public class SubjectsResource extends AbstractPscResource {

    private SubjectService subjectService;
    private SubjectDao subjectDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("MM-dd-yyyy");


    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
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
    public static JSONObject createJSONSubject(Subject subject) throws ResourceException {
        try {
            JSONObject jsonSubject = new JSONObject();

            JSONObject jsonStudySiteInfoObject = new JSONObject();
            JSONArray jsonStudySiteInfoArray = new JSONArray();
            List<StudySubjectAssignment> studySubjectAssignments = subject.getAssignments();
            for(StudySubjectAssignment studySubjectAssignemt : studySubjectAssignments) {
                Date startDate = studySubjectAssignemt.getStartDateEpoch();
                Date endDate = studySubjectAssignemt.getEndDateEpoch();
                jsonStudySiteInfoObject.put("site", studySubjectAssignemt.getStudySite().getSite().getName());
                jsonStudySiteInfoObject.put("study", studySubjectAssignemt.getStudySite().getStudy().getName());
                if (startDate != null){
                    jsonStudySiteInfoObject.put("start_date", dayFormatter.format(startDate));
                } else {
                    jsonStudySiteInfoObject.put("start_date", "");
                }

                if (endDate != null) {
                    jsonStudySiteInfoObject.put("end_date", dayFormatter.format(endDate));
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
                jsonSubject.put("date_of_birth", dayFormatter.format(subject.getDateOfBirth()));
            } else {
                jsonSubject.put("date_of_birth", "");
            }
            jsonSubject.put("gender", subject.getGender().getDisplayName());
            jsonSubject.put("person_id", subject.getPersonId());

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
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }
}


