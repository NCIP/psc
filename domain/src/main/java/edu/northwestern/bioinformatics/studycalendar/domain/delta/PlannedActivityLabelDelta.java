/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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
@DiscriminatorValue(value="label")
@SuppressWarnings({ "UnusedDeclaration" })
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
