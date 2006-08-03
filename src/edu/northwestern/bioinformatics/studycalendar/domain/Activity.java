package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.persistence.Basic;
import javax.persistence.SequenceGenerator;
import javax.persistence.Column;
import javax.persistence.FetchType;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "activities")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_activities_id")
    }
)
public class Activity extends AbstractDomainObject implements Comparable<Activity> {
    private String name;
    private String description;
    private ActivityType type;

    ///// LOGIC

    public int compareTo(Activity o) {
        // by type first
        int typeDiff = getType().compareTo(o.getType());
        if (typeDiff != 0) return typeDiff;
        // then by name
        return ComparisonUtils.nullSafeCompare(getName(), o.getName());
    }

    ///// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_type_id")
    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }
    
}
