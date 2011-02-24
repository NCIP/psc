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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubjectProperty that = (SubjectProperty) o;

        if (!name.equals(that.getName())) return false;
        if (!value.equals(that.getValue())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
