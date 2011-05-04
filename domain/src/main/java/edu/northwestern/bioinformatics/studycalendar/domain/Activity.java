package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.annotations.*;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "activities")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_activities_id")
    }
)
public class Activity extends AbstractMutableDomainObject
    implements Comparable<Activity>, Named, NaturallyKeyed, UniquelyKeyed, Cloneable, TransientCloneable<Activity>
{
    private static final Pattern ESCAPED_PIPE = Pattern.compile("\\\\\\|");
    private static final Pattern PIPE = Pattern.compile("\\|");

    private String name;
    private String description;
    private ActivityType activityType;
    private Source source;
    private String code;
    private List<ActivityProperty> properties = new ArrayList<ActivityProperty>();

    private boolean memoryOnly;
    private SortedSet<PlannedActivity> plannedActivities =new TreeSet<PlannedActivity>();

    ///// LOGIC

    public int compareTo(Activity o) {
        // by type first
        int typeDiff = ComparisonTools.nullSafeCompare(this.getType(), o.getType());
        if (typeDiff != 0) return typeDiff;
        // then by name
        return ComparisonTools.nullSafeCompare(toLower(getName()), toLower(o.getName()));
    }

    private String toLower(String name) {
        return name == null ? null : name.toLowerCase();
    }

    public void addProperty(ActivityProperty activityProperty) {
        getProperties().add(activityProperty);
        activityProperty.setActivity(this);
    }

    @Transient
    public String getNaturalKey() {
        return getCode();
    }

    @Transient
    public String getUniqueKey() {
        return new StringBuilder().
            append(escapePipe(getSource().getNaturalKey())).append("|").
            append(escapePipe(getNaturalKey())).
            toString();
    }

    private static String escapePipe(String value) {
        return value == null ? null : PIPE.matcher(value).replaceAll("\\\\|");
    }

    public static Map<String, String> splitPropertyChangeKey(String key) {
        String[] split = PIPE.split(ESCAPED_PIPE.matcher(key).replaceAll("\0"));
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("\0", "|");
        }
        Map<String, String> values = new HashMap<String, String>();
        values.put("source", split[0]);
        values.put("code", split[1]);
        return values;
    }

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public Activity transientClone() {
        Activity clone = clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    @Transient
    public boolean isDeletable() {
        return getPlannedActivities().size() <= 0;
    }


    @OneToMany(mappedBy = "activity")
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL })
    @Sort(type = SortType.NATURAL)
    public SortedSet<PlannedActivity> getPlannedActivities() {
        return plannedActivities;
    }

    public void setPlannedActivities(SortedSet<PlannedActivity> plannedActivities){
        this.plannedActivities = plannedActivities;
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

    @ManyToOne
    @JoinColumn(name = "activity_type_id")
    public ActivityType getType() {
        return activityType;
    }

    public void setType(ActivityType activityType) {
        this.activityType = activityType;
    }

    @OneToMany(mappedBy = "activity")
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL })
    public List<ActivityProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ActivityProperty> properties) {
        this.properties = properties;
    }

    public Differences deepEquals(Object o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("not an instance of activity");
            return differences;
        }
        Activity activity = (Activity) o;

        differences.registerValueDifference("activity name", getName(), activity.getName());
        differences.registerValueDifference("activity code", getCode(), activity.getCode());
        differences.registerValueDifference(
            "activity description", getDescription(), activity.getDescription());

        String prefix = String.format("Activity %s", name);

        if (source != null && activity.getSource()!= null) {
            Differences sourceDifferences = source.deepEquals(activity.getSource());
            if (sourceDifferences.hasDifferences()) {
                differences.addChildDifferences(prefix, sourceDifferences);
            }
        }

        if (activityType !=null && activity.getType() != null) {
            Differences typeDifferences = activityType.deepEquals(activity.getType());
            if (typeDifferences.hasDifferences()) {
                differences.addChildDifferences(prefix, typeDifferences);
            }
        }

        if (getProperties() != null && activity.getProperties() != null) {
            if (getProperties().size() != activity.getProperties().size()) {
                differences.addMessage(String.format("total no. of activity properties %d differs to %d",
                        getProperties().size(), activity.getProperties().size()));
            } else {
                for (int i=0; i<getProperties().size(); i++) {
                    ActivityProperty ap1 =  getProperties().get(i);
                    ActivityProperty ap2 = activity.getProperties().get(i);
                    Differences propertyDifferences = ap1.deepEquals(ap2);
                    if (propertyDifferences.hasDifferences()) {
                        differences.addChildDifferences(prefix, propertyDifferences);
                    }
                }
            }
        }
        return differences;
    }

    ////// OBJECT METHODS

    @Override
    @SuppressWarnings({ "unchecked" })
    public Activity clone() {
        try {
            Activity clone = (Activity) super.clone();
            clone.setSource(null);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Activity activity = (Activity) o;

        return ComparisonTools.nullSafeEquals(getSource(), activity.getSource())
            && ComparisonTools.nullSafeEquals(getNaturalKey(), activity.getNaturalKey());
    }

    public int hashCode() {
        int result;
        result = (getSource() != null ? getSource().hashCode() : 0);
        result = 31 * result + (getNaturalKey() != null ? getNaturalKey().hashCode() : 0);
        return result;
    }

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
                .append("[id=").append(getId())
                .append("; name=").append(getName())
                .append("; type=").append(getType())
                .append(']')
                .toString();
    }

    /**
     * finds the activity in collection which has the same code.
     *
     * @param activities collection of activities to match
     * @return activity if finds any activity in collection matching the same code. null if no match found
     */
    @Transient
    public Activity findActivityInCollectionWhichHasSameCode(List<Activity> activities) {
        for (Activity activitytoFind : activities) {

            if (this.getCode()!=null && this.getCode().equals(activitytoFind.getCode())) {
                return activitytoFind;
            }

        }

        return null;
    }

    /**
     * finds the activity in collection which has the same name.
     *
     * @param activities collection of activities to match
     * @return activity if finds any activity in collection matching the same name. null if no match found
     */
    @Transient
    public Activity findActivityInCollectionWhichHasSameName(List<Activity> activities) {
        for (Activity activitytoFind : activities) {
            if (this.getName()!=null && this.getName().equals(activitytoFind.getName())) {
                return activitytoFind;
            }
        }
        return null;
    }

    /**
     * updates the properties of activity.
     *
     * @param activity source activity
     */
    @Transient
    public void updateActivity(final Activity activity) {
        BeanUtils.copyProperties(activity, this, new String[]{"source", "id"});
    }
}
