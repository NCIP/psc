package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;
import java.util.Set;

import org.hibernate.validator.NotNull;

/**
 * @author Rhett Sutphin
 */
@Entity // TODO
@DiscriminatorValue("add")
public class Add extends Change {
    private String oldValue;
    private Integer newChildId;
    private Integer index;

    @Override
    @Transient
    public ChangeAction getAction() { return ChangeAction.ADD; }

    ////// BEAN PROPERTIES
    @Column (name = "new_value")
    public Integer getNewChildId() {
        return newChildId;
    }

    public void setNewChildId(Integer newChildId) {
        this.newChildId = newChildId;
    }
    @Column (name = "attribute")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }


    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }


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
