package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;

/**
 * A {@link Change} representing the reordering of the node's children.  Specifically,
 * it defines the move of a child from one index (position) to another in a list.
 *
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("reorder")
public class Reorder extends Change {
    private Integer newIndex;
    private Integer oldIndex;

    @Transient
    @Override
    public ChangeAction getAction() { return ChangeAction.REORDER; }

    ////// BEAN PROPERTIES

    @Column(name="new_value")
    public Integer getNewIndex() {
        return newIndex;
    }

    public void setNewIndex(Integer newIndex) {
        this.newIndex = newIndex;
    }

    @Column(name="old_value")
    public Integer getOldIndex() {
        return oldIndex;
    }

    public void setOldIndex(Integer oldIndex) {
        this.oldIndex = oldIndex;
    }
}
