/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;


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
    implements Named, NaturallyKeyed, Comparable<ActivityType>, DeepComparable<ActivityType>
{

    private String name;

    public ActivityType(String name){
        this.name = name;
    }

    public ActivityType() {

    }

    @Transient
    public String getSelector() {
        return "activity-type-" + getName().toLowerCase().replaceAll("\\s+", "_");
    }

    @Transient
    public String getNaturalKey() {
        return getName();
    }

    public int compareTo(ActivityType o) {
        return ComparisonTools.nullSafeCompare(toLower(getName()), toLower(o.getName()));
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

    public Differences deepEquals(ActivityType other) {
        Differences differences =  new Differences();
        differences.registerValueDifference("name", getName(), other.getName());
        return differences;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityType activityType = (ActivityType) o;

        return !(name != null ? !name.equals(activityType.getName()) : activityType.getName() != null);

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

