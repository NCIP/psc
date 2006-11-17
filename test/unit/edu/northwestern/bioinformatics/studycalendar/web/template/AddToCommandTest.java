package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AddToCommandTest extends StudyCalendarTestCase {
    private AddToCommand command = new AddToCommand();

    public void testStudyModePerformEdit() throws Exception {
        Study study = new Study();
        study.setPlannedCalendar(new PlannedCalendar());
        command.setStudy(study);

        command.performEdit();

        assertEquals(1, study.getPlannedCalendar().getEpochs().size());
        Epoch actualEpoch = study.getPlannedCalendar().getEpochs().get(0);
        assertEquals("Wrong name on new epoch", "[Unnamed epoch]", actualEpoch.getName());
        assertEquals("Epoch missing single arm", 1, actualEpoch.getArms().size());
    }

    public void testStudyModeModel() throws Exception {
        Study study = new Study();
        study.setPlannedCalendar(new PlannedCalendar());
        Epoch e1 = Epoch.create("E1");
        Epoch e2 = Epoch.create("E2");
        Epoch e3 = Epoch.create("[Unnamed epoch]");
        study.getPlannedCalendar().addEpoch(e1);
        study.getPlannedCalendar().addEpoch(e2);
        study.getPlannedCalendar().addEpoch(e3);
        command.setStudy(study);

        Map<String, Object> model = command.getModel();

        assertEquals(1, model.size());
        assertContainsPair("Missing epoch", model, "epoch", e3);
    }

    public void testEpochModePerformEdit() throws Exception {
        Study study = new Study();
        study.setPlannedCalendar(new PlannedCalendar());
        Epoch epoch = Epoch.create("Holocene", "A", "B");
        study.getPlannedCalendar().addEpoch(epoch);
        command.setEpoch(epoch);

        command.performEdit();

        assertEquals("New arm not added", 3, epoch.getArms().size());
        assertEquals("Wrong name for new arm", "[Unnamed arm]", epoch.getArms().get(2).getName());
    }
    
    public void testEpochModeModel() throws Exception {
        Epoch epoch = Epoch.create("E", "A", "[Unnamed arm]");
        command.setEpoch(epoch);

        Map<String, Object> model = command.getModel();
        assertEquals("Wrong number of model elements", 1, model.size());
        assertEquals("Wrong arm", epoch.getArms().get(1), model.get("arm"));
    }
}
