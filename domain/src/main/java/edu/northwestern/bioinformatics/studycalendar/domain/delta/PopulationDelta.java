/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;

import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nataliya Shurupova
 */

@Entity
@DiscriminatorValue(value="popltn")
public class PopulationDelta extends Delta<Population> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public PopulationDelta() { }

    public PopulationDelta(Population node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public Population getNode() {
        return super.getNode();
    }
}
