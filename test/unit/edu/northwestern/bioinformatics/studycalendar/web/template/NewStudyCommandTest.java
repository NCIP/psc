package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommandTest extends StudyCalendarTestCase {
    private NewStudyCommand command;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        command = new NewStudyCommand(studyDao);
    }

    public void testCreateBlank() throws Exception {
        command.setBase(NewStudyCommand.TemplateBase.BLANK);
        expectCreate();
        replayMocks();
        assertBlankStudy(command.create());
        verifyMocks();
    }
    
    private static void assertBlankStudy(Study actual) {
        assertEquals("Wrong study name", "[Unnamed blank study]", actual.getName());

        List<Epoch> epochs = actual.getPlannedCalendar().getEpochs();
        assertEquals("Wrong number of epochs", 1, epochs.size());
        assertEquals("Wrong epoch name", "[Unnamed epoch]", epochs.get(0).getName());
    }

    public void testCreateBasic() throws Exception {
        command.setBase(NewStudyCommand.TemplateBase.BASIC);
        expectCreate();
        replayMocks();
        assertBasicStudy(command.create());
        verifyMocks();
    }

    public void testDefaultIsBasic() throws Exception {
        command.setBase(null);
        expectCreate();
        replayMocks();
        assertBasicStudy(command.create());
        verifyMocks();
    }

    private static void assertBasicStudy(Study actual) {
        assertEquals("Wrong study name for new study", "[Unnamed study]", actual.getName());

        List<Epoch> epochs = actual.getPlannedCalendar().getEpochs();
        assertEquals("Wrong number of epochs", 3, epochs.size());
        assertEquals("Wrong name for epoch 0", "Screening", epochs.get(0).getName());
        assertEquals("Wrong name for epoch 1", "Treatment", epochs.get(1).getName());
        assertEquals("Wrong name for epoch 2", "Follow up", epochs.get(2).getName());

        List<Arm> treatmentArms = actual.getPlannedCalendar().getEpochs().get(1).getArms();
        assertEquals("Wrong name for treatment arm 0", "A", treatmentArms.get(0).getName());
        assertEquals("Wrong name for treatment arm 1", "B", treatmentArms.get(1).getName());
        assertEquals("Wrong name for treatment arm 2", "C", treatmentArms.get(2).getName());
    }

    private void expectCreate() {
        studyDao.save((Study) notNull());
    }
}
