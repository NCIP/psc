package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;
import java.util.Set;

import org.hibernate.validator.NotNull;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("add")
public class Add extends Change {
    private PlanTreeNode<?> newChild;
    private Integer newChildId; // package level for testing
    private Integer index;

    ////// LOGIC

    @Override
    @Transient
    public ChangeAction getAction() { return ChangeAction.ADD; }

    @Column (name = "new_value")
    public Integer getNewChildId() {
        if (getNewChild() != null) {
            return getNewChild().getId();
        } else {
            return newChildId;
        }
    }

    public void setNewChildId(Integer newChildId) {
        this.newChildId = newChildId;
        if (getNewChild() != null && !newChildId.equals(getNewChild().getId())) {
            setNewChild(null);
        }
    }

    ////// BEAN PROPERTIES

    @Transient
    public PlanTreeNode<?> getNewChild() {
        return newChild;
    }

    public void setNewChild(PlanTreeNode<?> newChild) {
        this.newChild = newChild;
    }

    @Column (name = "attribute")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append("; child id ").append(getNewChildId());
        if (getIndex() != null) {
            sb.append(" at index ").append(getIndex());
        }
        return sb.append(']').toString();
    }
}
