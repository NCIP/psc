package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

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
@DiscriminatorValue(value="arm")
public class ArmDelta extends Delta<Arm> {
    public ArmDelta() { }

    public ArmDelta(Arm node) { super(node); }

    @ManyToOne
    @JoinColumn(name = "node_id")
    @Override
    public Arm getNode() {
        return super.getNode();
    }
}
