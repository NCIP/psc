package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.IndexColumn;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "studies")
@SequenceGenerator(
    name = "SEQ",
    sequenceName = "seq_studies_id"
)
public class Study extends AbstractDomainObject {
    private String name;
    private List<Arm> arms = new ArrayList<Arm>();

    public void addArm(Arm arm) {
        arms.add(arm);
        arm.setStudy(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany (mappedBy = "study")
    @IndexColumn (name = "num", base = 1)
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }
}
