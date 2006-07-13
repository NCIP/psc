package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 */
public class Arm extends AbstractDomainObject {
    private Study study;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
