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
public class Label extends AbstractMutableDomainObject implements Named, Comparable<Label> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Label o) {
        int nameDiff = String.CASE_INSENSITIVE_ORDER.compare(this.getName(), o.getName());
        if (nameDiff != 0) {
            return nameDiff;
        } else {
            return this.getName().compareTo(o.getName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label)) return false;

        Label label = (Label) o;

        return getName().equals(label.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).
            append('[').append(getName()).append(']').
            toString();
    }
}
