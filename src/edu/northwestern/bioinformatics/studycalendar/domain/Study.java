package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "studies")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_studies_id")
    }
)
public class Study extends AbstractDomainObject {
    private String name;
    private List<Arm> arms = new ArrayList<Arm>();
    private boolean complete;
    
    public Study() {
    	setComplete(false);
    }

    ////// LOGIC
    

    public void addArm(Arm arm) {
        arms.add(arm);
        arm.setStudy(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Arm arm : arms) {
            len = Math.max(len, arm.getLengthInDays());
        }
        return len;
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isComplete() {
    	return complete;
    }

    public void setComplete(boolean complete) {
    	this.complete = complete;
    }

    @OneToMany (mappedBy = "study")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }
}
