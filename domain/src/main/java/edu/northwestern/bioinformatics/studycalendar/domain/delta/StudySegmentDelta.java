/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value="segmnt")
public class StudySegmentDelta extends Delta<StudySegment> {
    public StudySegmentDelta() { }

    public StudySegmentDelta(StudySegment node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public StudySegment getNode() {
        return super.getNode();
    }
}
