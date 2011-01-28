package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.Embeddable;

/**
 * @author Rhett Sutphin
 */
@Embeddable
public class SubjectProperty {
    private String name, value;

    public SubjectProperty() {
    }

    public SubjectProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
