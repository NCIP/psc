package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "sources")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
                @Parameter(name = "sequence", value = "seq_sources_id")
        }
)
public class Source extends AbstractMutableDomainObject
    implements Named, NaturallyKeyed, TransientCloneable<Source>, DeepComparable<Source>, Serializable
{
    private String name;
    private List<Activity> activities = new ArrayList<Activity>();
    private boolean memoryOnly;
    private Boolean manualFlag;

    ////// LOGIC

    @Transient
    public String getNaturalKey() {
        return getName();
    }

    public void addActivity(Activity activity) {
        getActivities().add(activity);
        activity.setSource(this);
    }

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public Source transientClone() {
        Source clone = new Source();
        clone.setName(getName());
        clone.setMemoryOnly(true);
        return clone;
    }

    @Transient
    public boolean isManualActivityTarget() {
        return getManualFlag();
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getManualFlag() {
        return manualFlag;
    }

    public void setManualFlag(Boolean manualFlag) {
        this.manualFlag = manualFlag;
    }

    @OneToMany(mappedBy = "source")
    @Cascade(value = {CascadeType.ALL})
    @OrderBy
    // ensure consistent ordering
    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public Differences deepEquals(Source other) {
        Differences differences = new Differences();
        differences.registerValueDifference("name", getName(), other.getName());
        return differences;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        return !(name != null ? !name.equals(source.getName()) : source.getName() != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[name=").append(getName()).append(']').
            toString();
    }

    /**
     * DO NOT CALLL THIS METHOD DIRECTLY. instead use SourceService#updateSource
     * <p>  add new activities  to the source. It does  update/delete any activitiy.
     * <li>
     * Add any activities that do not already exist.   </li>
     * </p>
     *
     * @param activities activities which may be added
     */
    @Transient
    public void addNewActivities(final List<Activity> activities) {
        List<Activity> existingActivities = getActivities();
        for (Activity activity : activities) {
            // check for new activity
            Activity existingActivity = activity.findActivityInCollectionWhichHasSameCode(existingActivities);
            if (existingActivity == null) {
                this.addActivity(activity);

            }
        }
    }

    public Source findSourceWhichHasSameName(Collection<Source> sources) {
        if (getName() == null) { return null; }

        for (Source s : sources) {
            if (getName().equals(s.getName())) {
                return s;
            }
        }
        return null;
    }
}
