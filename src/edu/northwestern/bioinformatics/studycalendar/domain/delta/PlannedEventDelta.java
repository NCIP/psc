package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="event")
public class PlannedEventDelta extends Delta<PlannedEvent> {
    public PlannedEventDelta() { }

    public PlannedEventDelta(PlannedEvent node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public PlannedEvent getNode() {
        return super.getNode();
    }
}
