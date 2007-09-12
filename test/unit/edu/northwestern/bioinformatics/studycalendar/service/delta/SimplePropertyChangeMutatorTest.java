package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

/**
 * @author Rhett Sutphin
 */
public class SimplePropertyChangeMutatorTest extends StudyCalendarTestCase {
    private Period period;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = new Period();
        period.setName("Autumn");
        period.setStartDay(1);
        period.getDuration().setUnit(Duration.Unit.day);
    }

    public void testApplyIntegerProperty() throws Exception {
        doApply(createPropertyChange("startDay", "1", "-1"), period);
        assertEquals("Property not updated", -1, (int) period.getStartDay());
    }

    public void testApplyStringProperty() throws Exception {
        doApply(createPropertyChange("name", "Autumn", "Winter"), period);
        assertEquals("Property not updated", "Winter", period.getName());
    }

    public void testApplyNullProperty() throws Exception {
        doApply(createPropertyChange("name", "Autumn", null), period);
        assertEquals("Property not updated", null, period.getName());
    }

    public void testApplyNestedProperty() throws Exception {
        doApply(createPropertyChange("duration.quantity", "4", "8"), period);
        assertEquals("Property not updated", 8, (int) period.getDuration().getQuantity());
    }

    public void testApplyEnumProperty() throws Exception {
        doApply(createPropertyChange("duration.unit", "day", "week"), period);
        assertEquals("Property not updated", Duration.Unit.week, period.getDuration().getUnit());
    }

    public void testRevertStringProperty() throws Exception {
        new SimplePropertyChangeMutator(
            createPropertyChange("name", "Spring", "Autumn")
        ).revert(period);
        assertEquals("Property not reverted", "Spring", period.getName());
    }
    
    private void doApply(PropertyChange change, PlanTreeNode<?> target) {
        new SimplePropertyChangeMutator(change).apply(target);
    }
}
