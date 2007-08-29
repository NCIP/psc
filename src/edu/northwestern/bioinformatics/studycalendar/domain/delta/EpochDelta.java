package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="1")
public class EpochDelta extends Delta<Epoch> {
    public EpochDelta() { }

    public EpochDelta(Epoch node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public Epoch getNode() {
        return super.getNode();
    }
}
