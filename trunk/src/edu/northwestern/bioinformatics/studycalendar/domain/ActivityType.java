package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Basic;
import javax.persistence.SequenceGenerator;
import javax.persistence.Column;
import javax.persistence.FetchType;
import java.util.Comparator;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "activity_types")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_activities_id")
    }
)
public class ActivityType extends AbstractDomainObject implements Comparable<ActivityType> {
    private static final Comparator<ActivityType> NATURAL_ORDER = new ById<ActivityType>();

    private String name;

    ////// LOGIC

    public int compareTo(ActivityType o) {
        return NATURAL_ORDER.compare(this, o);
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
