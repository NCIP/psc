package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("property")
public class PropertyChange extends Change {
    private String oldValue;
    private String newValue;
    private String propertyName;

    @Override
    @Transient
    public ChangeAction getAction() {
        return ChangeAction.CHANGE_PROPERTY;
    }

    ////// BEAN PROPERTIES

    @Column(name="old_value")
    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    @Column(name="new_value")
    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Column(name="attribute")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
