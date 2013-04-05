/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nataliya Shurupova
 */
@Entity
@DiscriminatorValue(value="study")
public class StudyDelta extends Delta<Study> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public StudyDelta() { }

    public StudyDelta(Study node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public Study getNode() {
        return super.getNode();
    }
}
