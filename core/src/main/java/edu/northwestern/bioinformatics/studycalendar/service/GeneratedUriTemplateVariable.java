package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import java.util.Map;
import java.util.HashMap;

public enum GeneratedUriTemplateVariable {
    STUDY_IDENTIFIER("study.assignedIdentifier"),
    ASSIGNMENT_IDENTIFIER("studySubjectAssignment.gridId"),
    SUBJECT_IDENTIFIER("subject.personId", "subject.gridId"),
    SCHEDULED_ACTIVITY_IDENTIFIER("scheduledActivity.gridId"),
    ACTIVITY_CODE("scheduledActivity.activity.code"),
    DAY_FROM_STUDY_PLAN("scheduledActivity.dayNumber"),
    STUDY_SUBJECT_IDENTIFIER("studySubjectAssignment.studySubjectId"),
    SITE_NAME("site.name"),
    SITE_IDENTIFIER("site.assignedIdentifier")
    ;

    private String resolutionPath;
    private String secondTryResolutionPath;

    GeneratedUriTemplateVariable(String resolutionPath) {
        this.resolutionPath = resolutionPath;
    }

    GeneratedUriTemplateVariable(String resolutionPath, String secondTryResolutionPath) {
        this.resolutionPath = resolutionPath;
        this.secondTryResolutionPath = secondTryResolutionPath;
    }

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public Object resolve(DomainContext context) {
        if (resolutionPath.equals("subject.personId")){
            if (context.getProperty(resolutionPath) == null || context.getProperty(resolutionPath).toString().trim().length() == 0) {
                return context.getProperty(secondTryResolutionPath);
            }
        }
        return context.getProperty(resolutionPath);
    }

    public static Map<String, Object> getAllTemplateValues(DomainContext context) {
        Map<String, Object> all = new HashMap<String, Object>();
        for (GeneratedUriTemplateVariable variable : values()) {
            all.put(variable.attributeName(), variable.resolve(context));
        }
        return all;
    }

    public static Map<String, Object> getAllTemplateValues(DomainContext context, StudySubjectAssignment assignment) {
        if (assignment != null) {
            context.setStudySubjectAssignment(assignment);
        }
        return getAllTemplateValues(context);
    }
}
