package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

public class EpochDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    public static final String EPOCH_DELTA = "epoch-delta";

    protected Changeable nodeInstance() {
        return new Epoch();
    }

    protected Delta deltaInstance() {
        return new EpochDelta();
    }

    protected String elementName() {
        return EPOCH_DELTA;
    }
}
