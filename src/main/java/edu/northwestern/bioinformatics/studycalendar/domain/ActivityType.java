package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import edu.nwu.bioinformatics.commons.ComparisonUtils;


/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "activity_types")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_activity_types_id")
    }
)
public class ActivityType extends AbstractMutableDomainObject
        implements Named, NaturallyKeyed, Comparable<ActivityType> {

    private String name;

    public ActivityType(String name){
        this.name = name;
    }

    public ActivityType() {

    }

    @Transient
    public String getNaturalKey() {
        return getName();
    }


    public int compareTo(ActivityType o) {
        return ComparisonUtils.nullSafeCompare(toLower(getName()), toLower(o.getName()));
    }

    private String toLower(String name) {
        return name == null ? null : name.toLowerCase();
    }
    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityType activityType = (ActivityType) o;

        return !(name != null ? !name.equals(activityType.name) : activityType.name != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }


    @Override
    public String toString() {
      return new StringBuilder(getClass().getSimpleName())
                .append("[id=").append(getId())
                .append("; name=").append(getName())
                .append(']')
                .toString();
    }
}

