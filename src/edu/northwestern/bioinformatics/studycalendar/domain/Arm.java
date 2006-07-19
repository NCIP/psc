package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Basic;
import javax.persistence.SequenceGenerator;
import javax.persistence.Column;
import javax.persistence.FetchType;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "arms")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_arms_id")
    }
)
public class Arm extends AbstractDomainObject {
    private Study study;
    private Integer number;
    private String name;

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

    @Basic
    @Column (name = "num")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
