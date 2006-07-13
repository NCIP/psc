package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name="studies")
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

    @Transient // TODO
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }
}
