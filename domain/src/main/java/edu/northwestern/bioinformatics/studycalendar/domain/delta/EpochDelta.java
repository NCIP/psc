/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="epoch")
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
