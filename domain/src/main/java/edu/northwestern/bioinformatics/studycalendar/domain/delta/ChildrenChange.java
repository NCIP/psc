/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import static edu.northwestern.bioinformatics.studycalendar.tools.StringTools.humanizeClassName;

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

    @Override
    public void setMemoryOnly(boolean memoryOnly) {
        super.setMemoryOnly(memoryOnly);
        if (getChild() != null) {
            getChild().setMemoryOnly(memoryOnly);
        }
    }

    @Transient
    public String getNaturalKey() {
        return String.format("%s of %s:%s",
            humanizeClassName(getClass().getSimpleName()),
            humanizeClassName(getChild().getClass().getSimpleName()),
            getChild().getGridId());
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

        if (getChild() != null ? !getChild().equals(that.getChild()) : that.getChild() != null) return false;
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
