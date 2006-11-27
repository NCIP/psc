package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class TemplateSkeletonCreatorTest extends StudyCalendarTestCase {

    public void testBlank() throws Exception {
        assertBlankStudy(TemplateSkeletonCreator.BLANK.create());
    }

    public void testBasic() throws Exception {
        assertBasicStudy(TemplateSkeletonCreator.BASIC.create());
    }
    
    public static void assertBlankStudy(Study actual) {
        assertEquals("Wrong study name", "[Unnamed blank study]", actual.getName());

        List<Epoch> epochs = actual.getPlannedCalendar().getEpochs();
        assertEquals("Wrong number of epochs", 1, epochs.size());
        assertEquals("Wrong epoch name", "[Unnamed epoch]", epochs.get(0).getName());
    }

    public static void assertBasicStudy(Study actual) {
        assertEquals("Wrong study name for new study", "[Unnamed study]", actual.getName());

        PlannedCalendar calendar = actual.getPlannedCalendar();
        assertBasicPlannedCalendar(calendar);
    }

    public static void assertBasicPlannedCalendar(PlannedCalendar calendar) {
        List<Epoch> epochs = calendar.getEpochs();
        assertEquals("Wrong number of epochs", 3, epochs.size());
        assertEquals("Wrong name for epoch 0", "Screening", epochs.get(0).getName());
        assertEquals("Wrong name for epoch 1", "Treatment", epochs.get(1).getName());
        assertEquals("Wrong name for epoch 2", "Follow up", epochs.get(2).getName());

        List<Arm> treatmentArms = calendar.getEpochs().get(1).getArms();
        assertEquals("Wrong name for treatment arm 0", "A", treatmentArms.get(0).getName());
        assertEquals("Wrong name for treatment arm 1", "B", treatmentArms.get(1).getName());
        assertEquals("Wrong name for treatment arm 2", "C", treatmentArms.get(2).getName());
    }

}
