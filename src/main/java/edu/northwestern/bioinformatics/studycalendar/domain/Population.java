package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Comparator;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
                @Parameter(name = "sequence", value = "seq_populations_id")
        }
)
public class Population extends AbstractMutableDomainObject implements Named, NaturallyKeyed, Comparable<Population>, Cloneable {
    private Study study;
    private String name;
    private String abbreviation;

    @SuppressWarnings({"unchecked"})
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


    @Override
    public Population clone() {
        try {
            Population clone = (Population) super.clone();
            clone.setStudy(null);
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    public static Population findMatchingPopulationByAbbreviation(final Set<Population> populations, final Population population) {
        if (population != null) {

            for (Population matchingPopulation : populations) {

                if (StringUtils.equals(matchingPopulation.getAbbreviation(), population.getAbbreviation())) {
                    return matchingPopulation;
                }
            }
        }
        return null;


    }
}
