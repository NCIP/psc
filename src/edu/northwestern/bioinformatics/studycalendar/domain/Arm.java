package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "arms")
public class Arm extends AbstractDomainObject {
    private Study study;
    private Integer index;
    private String name;

    @Transient
    public Integer getNumber() {
        return getIndex() == null ? null : getIndex() + 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne
    @JoinColumn (name = "study_id")
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    private Integer getIndex() {
        return index;
    }

    private void setIndex(Integer index) {
        this.index = index;
    }
}
