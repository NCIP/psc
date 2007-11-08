package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="event")
public class PlannedEventDelta extends Delta<PlannedActivity> {
    public PlannedEventDelta() { }

    public PlannedEventDelta(PlannedActivity node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public PlannedActivity getNode() {
        return super.getNode();
    }
}
