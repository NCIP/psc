package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import java.util.Map;
import java.util.HashMap;

public enum GeneratedUriTemplateVariable {
    STUDY_IDENTIFIER("study.assignedIdentifier"),
    ASSIGNMENT_IDENTIFIER("studySubjectAssignment.gridId"),
    SUBJECT_IDENTIFIER("subject.personId", "subject.gridId");

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

    public Object resolve(BreadcrumbContext context) {
        if (resolutionPath.equals("subject.personId")){
            if (context.getProperty(resolutionPath) == null || context.getProperty(resolutionPath).toString().trim().length() == 0) {
                return context.getProperty(secondTryResolutionPath);
            }
        }
        return context.getProperty(resolutionPath);
    }

    public static Map<String, Object> getAllTemplateValues(BreadcrumbContext context) {
        Map<String, Object> all = new HashMap<String, Object>();
        for (GeneratedUriTemplateVariable variable : values()) {
            all.put(variable.attributeName(), variable.resolve(context));
        }
        return all;
    }

    public static Map<String, Object> getAllTemplateValues(BreadcrumbContext context, StudySubjectAssignment assignment) {
        if (assignment != null) {
            context.setStudySubjectAssignment(assignment);
        }
        return getAllTemplateValues(context);
    }
}
