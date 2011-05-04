package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
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
public class Population extends AbstractMutableDomainObject
    implements Named, NaturallyKeyed, Child<Study>, Comparable<Population>, Cloneable
{
    private Study study;
    private String name;
    private String abbreviation;
    private boolean memoryOnly;

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

    ////// IMPLEMENTATION OF Child

    public Class<Study> parentClass() {
        return Study.class;
    }

    public void setParent(Study parent) {
        setStudy(parent);
    }

    @Transient
    public Study getParent() {
        return getStudy();
    }

    public Differences deepEquals(Object o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || !(o instanceof Population)) {
            differences.addMessage("not an instance of Population");
            return differences;
        }

        Population that = (Population) o;

        return differences.registerValueDifference("name", getName(), that.getName()).
            registerValueDifference("abbreviation", getAbbreviation(), that.getAbbreviation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Population)) return false;

        Population that = (Population) o;

        if (name != null ? !name.equals(that.getName()) : that.getName() != null)
            return false;
        if (abbreviation != null ? !abbreviation.equals(that.getAbbreviation()) : that.getAbbreviation() != null)
            return false;
        return true;
    }


    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public Population transientClone() {
        Population clone = clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Population clone() {
        try {
            Population clone = (Population) super.clone();
            clone.setStudy(null);
            return clone;
        } catch (CloneNotSupportedException e) {
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

    @Transient
    public boolean isDetached() {
        return getStudy() == null;
    }
}
