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
@DiscriminatorValue(value="studysegment")
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
