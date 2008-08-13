package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.collections.comparators.NullComparator;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_populations_id")
    }
)
public class Population extends AbstractMutableDomainObject implements Named, NaturallyKeyed, Comparable<Population> {
    private Study study;
    private String name;
    private String abbreviation;

    @SuppressWarnings({ "unchecked" })
    private static Comparator<String> NAME_COMPARATOR =
        new NullComparator(String.CASE_INSENSITIVE_ORDER);

    ////// LOGIC

    @Transient
    public String getNaturalKey() {
        return getAbbreviation();
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public int compareTo(Population o) {
        return NAME_COMPARATOR.compare(this.getName(), o.getName());
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append('[').append(getId())
            .append(" | ").append(getAbbreviation())
            .append(": ").append(getName())
            .append(']').toString();
    }
}
