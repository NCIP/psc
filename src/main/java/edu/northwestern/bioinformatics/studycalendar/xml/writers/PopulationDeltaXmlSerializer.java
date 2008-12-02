package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PopulationDelta;

/**
 * @author Nataliya Shurupova
 */
public class PopulationDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    public static final String POPULATION_DELTA = "population-delta";

    //not sure about this method
    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedCalendar();
    }

    protected Delta deltaInstance() {
        return new PopulationDelta();
    }

    protected String elementName() {
        return POPULATION_DELTA;
    }
}

