package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sources")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_sources_id")
    }
)
public class Source extends AbstractMutableDomainObject implements Named, NaturallyKeyed {
    private String name;
    private List<Activity> activities = new ArrayList<Activity>();

    ////// LOGIC

    @Transient
    public String getNaturalKey() {
        return getName();
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "source")
    @Cascade(value = { CascadeType.ALL })
    @OrderBy // ensure consistent ordering
    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        return !(name != null ? !name.equals(source.name) : source.name != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }
}
