/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
import edu.northwestern.bioinformatics.studycalendar.domain.UniquelyKeyed;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

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
        change.setOldValue(serializeValue(oldValue));
        change.setNewValue(serializeValue(newValue));
        return change;
    }

    private static String serializeValue(Object value) {
        if (value instanceof UniquelyKeyed) {
            return ((UniquelyKeyed) value).getUniqueKey();
        } else if (value instanceof NaturallyKeyed) {
            return ((NaturallyKeyed) value).getNaturalKey();
        } else {
            return value == null ? null : value.toString();
        }
    }

    ////// LOGIC

    @Override
    @Transient
    public boolean isNoop() {
        return ComparisonTools.nullSafeEquals(getOldValue(), getNewValue());
    }

    @Override
    @Transient
    public ChangeAction getAction() {
        return ChangeAction.CHANGE_PROPERTY;
    }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
        return new PropertyMergeLogic(delta, updateTime);
    }

    @Transient
    public String getNaturalKey() {
        return String.format("property change for %s", getPropertyName());
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

    @Override
    public Differences deepEquals(Change o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("Object is not instance of " +getClass());
            return differences;
        }

        PropertyChange that = (PropertyChange) o;

        return differences.
            registerValueDifference("property", this.getPropertyName(), that.getPropertyName()).
            registerValueDifference("new value", this.getNewValue(), that.getNewValue()).
            registerValueDifference("old value", this.getOldValue(), that.getOldValue())
            ;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyChange that = (PropertyChange) o;

        if (newValue != null ? !newValue.equals(that.getNewValue()) : that.getNewValue() != null)
            return false;
        if (oldValue != null ? !oldValue.equals(that.getOldValue()) : that.getOldValue() != null)
            return false;
        if (propertyName != null ? !propertyName.equals(that.getPropertyName()) : that.getPropertyName() != null)
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
        private Date updateTime;

        public PropertyMergeLogic(Delta<?> delta, Date updateTime) {
            this.delta = delta;
            this.updateTime = updateTime;
        }

        @Override
        public boolean encountered(PropertyChange change) {
            if (change.getPropertyName().equals(getPropertyName())) {
                change.setNewValue(getNewValue());
                change.setUpdatedDate(updateTime);
                return true;
            }
            return false;
        }

        @Override
        public void postProcess(boolean merged) {
            if (!merged) {
                PropertyChange.this.setUpdatedDate(updateTime);
                delta.addChange(PropertyChange.this);
            }
        }
    }
}
