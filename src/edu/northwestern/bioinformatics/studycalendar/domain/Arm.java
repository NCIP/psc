package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

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
    private String name;
    private Set<Period> periods = new HashSet<Period>();

    // business methods

    public void addPeriod(Period period) {
        periods.add(period);
        period.setArm(this);
    }

    // bean methods

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

    @OneToMany (mappedBy = "arm")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(Set<Period> periods) {
        this.periods = periods;
    }
}
