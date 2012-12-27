/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
@DiscriminatorValue(value="cal")
public class PlannedCalendarDelta extends Delta<PlannedCalendar> {
    public PlannedCalendarDelta() { }

    public PlannedCalendarDelta(PlannedCalendar node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public PlannedCalendar getNode() {
        return super.getNode();
    }
}
