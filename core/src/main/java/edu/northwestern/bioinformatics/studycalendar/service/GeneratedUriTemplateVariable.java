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

    // TODO: this special case should be handled elsewhere
    public static Map<String, Object> getAllTemplateValues(DomainContext context, StudySubjectAssignment assignment) {
        if (assignment != null) {
            // TODO: this is bad -- the context is potentially a shared object and must not be mutated
            context.setStudySubjectAssignment(assignment);
        }
        return getAllTemplateValues(context);
    }
}
