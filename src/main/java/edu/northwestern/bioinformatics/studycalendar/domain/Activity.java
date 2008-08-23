package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    implements Comparable<Activity>, Named, NaturallyKeyed, UniquelyKeyed
{
    private static final Pattern ESCAPED_PIPE = Pattern.compile("\\\\\\|");
    private static final Pattern PIPE = Pattern.compile("\\|");

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
     * updates the properties of activity.
     *
     * @param activity source activity
     */
    @Transient
    public void updateActivity(final Activity activity) {
        BeanUtils.copyProperties(activity, this, new String[]{"source", "id"});
    }
}
