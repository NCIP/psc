package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.persistence.Basic;
import javax.persistence.SequenceGenerator;
import javax.persistence.Column;
import javax.persistence.FetchType;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

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
public class Activity extends AbstractMutableDomainObject implements Comparable<Activity>, Named {
    private String name;
    private String description;
    private ActivityType type;
    private Source source;
    private String code;

    ///// LOGIC

    public int compareTo(Activity o) {
        // by type first
        int typeDiff = getType().compareTo(o.getType());
        if (typeDiff != 0) return typeDiff;
        // then by name
        return ComparisonUtils.nullSafeCompare(toLower(getName()), toLower(o.getName()));
    }

    private String toLower(String name) {
        return name == null ? null : name.toLowerCase();
    }

    ///// BEAN PROPERTIES

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    @ManyToOne
    @JoinColumn(name = "source_id")
    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Type(type = "activityType")
    @Column(name = "activity_type_id")
    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    ////// OBJECT METHODS

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId())
            .append("; name=").append(getName())
            .append("; type=").append(getType())
            .append(']')
            .toString();
    }
}
