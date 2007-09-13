package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "epochs")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_epochs_id")
    }
)
public class Epoch extends PlanTreeOrderedInnerNode<PlannedCalendar, Arm> implements Named {
    private String name;

    ////// FACTORY

    public static Epoch create(String epochName, String... armNames) {
        Epoch epoch = new Epoch();
        epoch.setName(epochName);
        if (armNames.length == 0) {
            epoch.addNewArm(epochName);
        } else {
            for (String armName : armNames) {
                epoch.addNewArm(armName);
            }
        }
        return epoch;
    }

    private void addNewArm(String armName) {
        Arm arm = new Arm();
        arm.setName(armName);
        addArm(arm);
    }

    ////// LOGIC

    @Override public Class<PlannedCalendar> parentClass() { return PlannedCalendar.class; }
    @Override public Class<Arm> childClass() { return Arm.class; }

    public void addArm(Arm arm) {
        addChild(arm);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Arm arm : getArms()) {
            len = Math.max(len, arm.getLengthInDays());
        }
        return len;
    }

    @Transient
    public boolean isMultipleArms() {
        return getArms().size() > 1;
    }

    ////// BEAN PROPERTIES

    // This is annotated this way so that the IndexColumn will work with
    // the bidirectional mapping.  See section 2.4.6.2.3 of the hibernate annotations docs.
    @OneToMany
    @JoinColumn(name="epoch_id")
    @IndexColumn(name="list_index")
    @Cascade(value = { CascadeType.ALL })
    public List<Arm> getArms() {
        return getChildren();
    }

    public void setArms(List<Arm> arms) {
        setChildren(arms);
    }

    // This is annotated this way so that the IndexColumn in the parent
    // will work with the bidirectional mapping
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(insertable=false, updatable=false, nullable=true)
    public PlannedCalendar getPlannedCalendar() {
        return getParent();
    }

    public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
        setParent(plannedCalendar);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
