/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AddToCommandTest extends EditCommandTestCase {
    private AddToCommand command = new AddToCommand();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Fixtures.assignIds(study);
        command.setDeltaService(getTestingDeltaService());
    }

    public void testStudyModePerformEdit() throws Exception {
        command.setStudy(study);
        command.performEdit();

        Add actualAdd = DeltaAssertions.assertChangeIsAdd("Bad change", lastChange());
        assertEquals("Added with wrong index", 0, (int) actualAdd.getIndex());
        Epoch actualEpoch = (Epoch) (PlanTreeNode) actualAdd.getChild(); // double cast for javac bug
        assertEquals("Wrong name on new epoch", "[Unnamed epoch]", actualEpoch.getName());
        assertEquals("Epoch missing single studySegment", 1, actualEpoch.getStudySegments().size());
    }

    public void testStudyModeModel() throws Exception {
        Epoch e1 = Epoch.create("E1");
        Epoch e2 = Epoch.create("E2");
        Epoch e3 = Epoch.create("[Unnamed epoch]");
        study.getPlannedCalendar().addEpoch(e1);
        study.getPlannedCalendar().addEpoch(e2);
        study.getDevelopmentAmendment().addDelta(
            Delta.createDeltaFor(study.getPlannedCalendar(), Add.create(e3, 2)));
        command.setStudy(study);

        Map<String, Object> model = command.getModel();

        assertEquals("Missing epoch", e3.getName(), ((Epoch) model.get("epoch")).getName());
    }

    public void testEpochModePerformEdit() throws Exception {
        Epoch epoch = Epoch.create("Holocene", "A", "B");
        study.getPlannedCalendar().addEpoch(epoch);
        Fixtures.assignIds(study);
        command.setEpoch(epoch);
        command.setStudy(study);

        assertEquals("Test setup failure", 2, epoch.getStudySegments().size());

        command.performEdit();

        assertEquals("Epoch directly modified: " + study.getAmendment(),
            2, epoch.getStudySegments().size());

        Add actualAdd = DeltaAssertions.assertChangeIsAdd("Bad change", lastChange());
        assertEquals("Added with wrong index", 2, (int) actualAdd.getIndex());
        StudySegment addedStudySegment = (StudySegment) (PlanTreeNode) actualAdd.getChild(); // double cast for javac bug
        assertEquals("Wrong name for new study segment", "[Unnamed study segment]", addedStudySegment.getName());
    }
    
    public void testEpochModeModel() throws Exception {
        Epoch epoch = Epoch.create("E1", "A", "B");
        study.getPlannedCalendar().addEpoch(epoch);
        StudySegment newStudySegment = epoch.getStudySegments().remove(1);
        study.getDevelopmentAmendment().addDelta(
            Delta.createDeltaFor(epoch, Add.create(newStudySegment, 1)));
        Fixtures.assignIds(study);

        command.setEpoch(epoch);
        command.setStudy(study);

        Map<String, Object> model = command.getModel();
        assertEquals("Wrong studySegment", newStudySegment.getName(), ((StudySegment) model.get("studySegment")).getName());
    }
}
