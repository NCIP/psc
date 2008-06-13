package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

@Entity
@Table(name = "labels")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_labels_id")
    }
)
public class Label extends AbstractMutableDomainObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    @OneToMany(mappedBy = "label")
//    @OrderBy
//    @Cascade(value = { org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.LOCK, org.hibernate.annotations.CascadeType.MERGE,
//            org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REFRESH, org.hibernate.annotations.CascadeType.REMOVE, org.hibernate.annotations.CascadeType.REPLICATE,
//            org.hibernate.annotations.CascadeType.SAVE_UPDATE })
//    public List<PlannedActivityLabel> getPlannedActivityLabels() {
//        return new ArrayList<PlannedActivityLabel>();
//    }



    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" Name = ");
        sb.append(getName());
        return sb.toString();
    }
}
