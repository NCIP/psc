/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="event")
public class PlannedActivityDelta extends Delta<PlannedActivity> {
    public PlannedActivityDelta() { }

    public PlannedActivityDelta(PlannedActivity node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public PlannedActivity getNode() {
        return super.getNode();
    }
}