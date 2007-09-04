package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("remove")
public class Remove extends Change {
    private Integer childId;

    @Override
    @Transient
    public ChangeAction getAction() { return ChangeAction.REMOVE; }

    ////// BEAN PROPERTIES

    @Column(name = "old_value")
    public Integer getChildId() {
        return childId;
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append("; child id=").append(getChildId());
        return sb.append(']').toString();
    }
}
