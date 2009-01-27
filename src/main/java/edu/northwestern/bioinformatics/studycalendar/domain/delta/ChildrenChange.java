package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author Rhett Sutphin
 */
@MappedSuperclass
public abstract class ChildrenChange extends Change {
    private Child<?> child;
    private Integer childId;

    ////// LOGIC

    @Transient
    public boolean isSameChild(ChildrenChange other) {
        if (getChildId() != null) {
            return getChildId().equals(other.getChildId());
        } else {
            return ComparisonTools.nullSafeEquals(this.getChild(), other.getChild());
        }
    }

    @Transient
    public boolean isSameChild(Child<?> candidate) {
        if (getChildId() != null) {
            return getChildId().equals(candidate.getId());
        } else {
            return ComparisonTools.nullSafeEquals(this.getChild(), candidate);
        }
    }

    public void setToSameChildAs(ChildrenChange other) {
        if (other.getChild() != null) {
            setChild(other.getChild());
        } else {
            setChild(null);
            setChildId(other.getChildId());
        }
    }

    @Transient
    public String getChildIdText() {
        return getChildId() == null ? null : getChildId().toString();
    }

    public void setChildIdText(String childIdText) {
        if (childIdText == null) {
            setChildId(null);
        } else {
            try {
                setChildId(new Integer(childIdText));
            } catch (NumberFormatException nfe) {
                throw new StudyCalendarSystemException("Child ID text must be a string representation of an integer, not %s", childIdText);
            }
        }
    }
    
    @Transient // here only -- mapped in subclasses
    public Integer getChildId() {
        if (getChild() != null) {
            return getChild().getId();
        } else {
            return childId;
        }
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
        if (getChild() != null && childId != null && !childId.equals(getChild().getId())) {
            setChild(null);
        }
    }

    ////// BEAN PROPERTIES

    @Transient
    public Child<?> getChild() {
        return child;
    }

    public void setChild(Child<?> newChild) {
        this.child = newChild;
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChildrenChange that = (ChildrenChange) o;

        if (getChild() != null ? !getChild().equals(that.child) : that.getChild() != null) return false;
        if (getChildId() != null ? !getChildId().equals(that.getChildId()) : that.getChildId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (getChild() != null ? getChild().hashCode() : 0);
        result = 31 * result + (getChildId() != null ? getChildId().hashCode() : 0);
        return result;
    }

    @Override
    public ChildrenChange clone() {
        ChildrenChange clone = (ChildrenChange) super.clone();
        if (getChild() != null) clone.setChild(getChild().clone());
        return clone;
    }
}
