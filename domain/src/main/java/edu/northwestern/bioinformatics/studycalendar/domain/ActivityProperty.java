/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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
public class ActivityProperty extends AbstractMutableDomainObject
    implements Cloneable, DeepComparable<ActivityProperty>, NaturallyKeyed
{
    private String namespace;
    private String name;
    private String value;
    private Activity activity;
    private boolean memoryOnly;

    ////// LOGIC

    @Transient
    public String getNaturalKey() {
        return String.format("%s:%s:%s", getNamespace(), getName(), getValue());
    }

    ////// BEAN PROPERTIES
    
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

    ////// OBJECT METHODS

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

    public Differences deepEquals(ActivityProperty that) {
        return new Differences().
            registerValueDifference("name", this.getName(), that.getName()).
            registerValueDifference("value", this.getValue(), that.getValue()).
            registerValueDifference("namespace", this.getNamespace(), that.getNamespace());
    }
}
