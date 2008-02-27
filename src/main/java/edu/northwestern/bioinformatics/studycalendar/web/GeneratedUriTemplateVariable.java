package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;

import java.util.Map;
import java.util.HashMap;

public enum GeneratedUriTemplateVariable {
    STUDY_IDENTIFIER("study.gridId"),
    ASSIGNMENT_IDENTIFIER("studySubjectAssignment.gridId");

    private String resolutionPath;

    GeneratedUriTemplateVariable(String resolutionPath) {
        this.resolutionPath = resolutionPath;
    }

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public Object resolve(BreadcrumbContext context) {
        return context.getProperty(resolutionPath);
    }

    public static Map<String, Object> getAllTemplateValues(BreadcrumbContext context) {
        Map<String, Object> all = new HashMap<String, Object>();
        for (GeneratedUriTemplateVariable variable : values()) {
            all.put(variable.attributeName(), variable.resolve(context));
        }
        return all;
    }
}                                                                                                                                        
