package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

/**
 * @author Jalpa Patel
 */
@Entity
@Table(name = "activity_properties")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
        @Parameter(name = "sequence", value = "seq_activity_properties_id")
                }
)
public class ActivityProperty extends AbstractMutableDomainObject implements Cloneable {
    private String namespace;
    private String name;
    private String value;
    private Activity activity;
    private boolean memoryOnly;

    //BEAN PROPERTIES
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    @Column(name = "namespace")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public ActivityProperty clone() {
        try {
            ActivityProperty clone = (ActivityProperty) super.clone();
            clone.setNamespace(this.namespace);
            clone.setName(this.name);
            clone.setValue(this.value);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Error when cloning ActivityProperty", e);
        }
    }

    public Differences deepEquals(Object o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("not an instance of activity property");
            return differences;
        }

        ActivityProperty activityProperty = (ActivityProperty) o;

        if (name != null ? !name.equals(activityProperty.name) : activityProperty.name != null) {
            differences.addMessage(String.format("ActivityProperty name %s differs to %s", name, activityProperty.name));
        }

        if (value != null ? !value.equals(activityProperty.value) : activityProperty.value != null) {
            differences.addMessage(String.format("ActivityProperty value %s differs to %s", value, activityProperty.value));
        }

        if (namespace != null ? !namespace.equals(activityProperty.namespace) : activityProperty.namespace != null) {
            differences.addMessage(String.format("ActivityProperty namespace %s differs to %s", namespace, activityProperty.namespace));
        }

        return differences;
    }
}
