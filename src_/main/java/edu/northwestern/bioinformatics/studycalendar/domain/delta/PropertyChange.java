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

    ////// FACTORY

    public static PropertyChange create(String prop, Object oldValue, Object newValue) {
        PropertyChange change = new PropertyChange();
        change.setPropertyName(prop);
        change.setOldValue(oldValue == null ? null : oldValue.toString());
        change.setNewValue(newValue == null ? null : newValue.toString());
        return change;
    }

    ////// LOGIC

    @Override
    @Transient
    public ChangeAction getAction() {
        return ChangeAction.CHANGE_PROPERTY;
    }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta) {
        return new PropertyMergeLogic(delta);
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

    @Column(name="attribute", nullable = false)
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append("; change property ").append(getPropertyName())
            .append(" from ").append(getOldValue()).append(" to ").append(getNewValue())
            .append(']')
            .toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyChange that = (PropertyChange) o;

        if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null)
            return false;
        if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null)
            return false;
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (oldValue != null ? oldValue.hashCode() : 0);
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
        return result;
    }

    private class PropertyMergeLogic extends MergeLogic {
        private Delta<?> delta;

        public PropertyMergeLogic(Delta<?> delta) {
            this.delta = delta;
        }

        @Override
        public boolean encountered(PropertyChange change) {
            if (change.getPropertyName().equals(getPropertyName())) {
                change.setNewValue(getNewValue());
                return true;
            }
            return false;
        }


        @Override
        public void postProcess(boolean merged) {
            if (!merged) {
                delta.getChanges().add(PropertyChange.this);
            }
        }
    }
}
