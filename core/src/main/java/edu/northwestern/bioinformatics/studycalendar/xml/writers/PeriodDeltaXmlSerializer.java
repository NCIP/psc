package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PeriodDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

public class PeriodDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    public static final String PERIOD_DELTA = "period-delta";

    protected Changeable nodeInstance() {
        return new Period();
    }

    protected Delta deltaInstance() {
        return new PeriodDelta();
    }

    protected String elementName() {
        return PERIOD_DELTA;
    }
}
