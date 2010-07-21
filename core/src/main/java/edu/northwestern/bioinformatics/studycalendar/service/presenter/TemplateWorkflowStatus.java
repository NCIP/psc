package edu.northwestern.bioinformatics.studycalendar.service.presenter;

/**
 * @author Rhett Sutphin
 */
public enum TemplateWorkflowStatus {
    IN_DEVELOPMENT("Undergoing template development"),
    PENDING("Released but not yet usable"),
    AVAILABLE("Released and available to assign subjects");

    private final String description;

    TemplateWorkflowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
