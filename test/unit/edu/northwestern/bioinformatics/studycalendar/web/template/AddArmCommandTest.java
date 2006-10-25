package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AddArmCommandTest extends StudyCalendarTestCase {
    private AddArmCommand command;

    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        command = new AddArmCommand(studyDao);
    }

    public void testApply() throws Exception {
        Study study = new Study();
        study.setPlannedCalendar(new PlannedCalendar());
        Epoch epoch = Epoch.create("Holocene", "A", "B");
        study.getPlannedCalendar().addEpoch(epoch);

        command.setEpoch(epoch);
        studyDao.save(study);

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("New arm not added", 3, epoch.getArms().size());
        assertEquals("Wrong name for new arm", "New arm", epoch.getArms().get(2).getName());
    }

    public void testModel() throws Exception {
        Epoch epoch = Epoch.create("E", "A", "New arm");
        command.setEpoch(epoch);

        Map<String, Object> model = command.getModel();
        assertEquals("Wrong number of model elements", 1, model.size());
        assertEquals("Wrong arm", epoch.getArms().get(1), model.get("arm"));
    }
}
