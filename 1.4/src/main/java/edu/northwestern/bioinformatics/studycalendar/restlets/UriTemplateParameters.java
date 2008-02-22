package edu.northwestern.bioinformatics.studycalendar.restlets;

/**
 * @author Rhett Sutphin
 */
public enum UriTemplateParameters {
    STUDY_IDENTIFIER,
    SOURCE_NAME;

    public String attributeName() {
        return name().toLowerCase();
    }
}
