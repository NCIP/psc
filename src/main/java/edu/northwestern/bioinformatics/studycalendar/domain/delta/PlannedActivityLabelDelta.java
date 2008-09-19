package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="paLabel")
public class PlannedActivityLabelDelta extends Delta<PlannedActivityLabel> {
    public PlannedActivityLabelDelta() { }

    public PlannedActivityLabelDelta(PlannedActivityLabel node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public PlannedActivityLabel getNode() {
        return super.getNode();
    }
}
