package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="2")
public class PlannedCalendarDelta extends Delta<PlannedCalendar> {
    public PlannedCalendarDelta() { }

    public PlannedCalendarDelta(PlannedCalendar node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public PlannedCalendar getNode() {
        return super.getNode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" to PlannedCalendar");
        return sb.toString();
    }
}
