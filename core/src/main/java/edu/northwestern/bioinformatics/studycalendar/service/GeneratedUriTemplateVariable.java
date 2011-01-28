package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.HashMap;
import java.util.Map;

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

    private String[] resolutionPaths;

    GeneratedUriTemplateVariable(String... resolutionPaths) {
        this.resolutionPaths = resolutionPaths;
    }

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public Object resolve(DomainContext context) {
        for (String resolutionPath : resolutionPaths) {
            Object result = context.getProperty(resolutionPath);
            if (result != null) return result;
        }
        return null;
    }

    public static Map<String, Object> getAllTemplateValues(DomainContext context) {
        Map<String, Object> all = new HashMap<String, Object>();
        for (GeneratedUriTemplateVariable variable : values()) {
            all.put(variable.attributeName(), variable.resolve(context));
        }
        return all;
    }
}
