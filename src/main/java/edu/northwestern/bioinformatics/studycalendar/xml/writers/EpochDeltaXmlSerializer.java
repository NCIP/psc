package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;

public class EpochDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    public static final String EPOCH_DELTA = "epoch-delta";

    public EpochDeltaXmlSerializer(Study study) {
        super(study);
    }

    protected PlanTreeNode<?> nodeInstance() {
        return new Epoch();
    }

    protected Delta deltaInstance() {
        return new EpochDelta();
    }

    protected String elementName() {
        return EPOCH_DELTA;
    }
}
