package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Rhett Sutphin
 */
public class EditCommandTest extends StudyCalendarTestCase {
    private EditCommand command;
    private StudyDao studyDao;

    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);

        command = registerMockFor(EditCommand.class,
            EditCommand.class.getDeclaredMethod("performEdit"),
            EditCommand.class.getDeclaredMethod("getRelativeViewName")
        );
        command.setStudyDao(studyDao);

        study = Fixtures.createSingleEpochStudy("Study 1234", "E1", "A", "B");
        study.getPlannedCalendar().addEpoch(Epoch.create("E2"));
    }

    public void testApply() throws Exception {
        command.setStudy(study);
        command.performEdit();
        studyDao.save(study);

        replayMocks();
        command.apply();
        verifyMocks();
    }
    
    public void testApplyToCompleteCalendar() throws Exception {
        study.getPlannedCalendar().setComplete(true);
        command.setStudy(study);

        try {
            command.apply();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertContains(e.getMessage(), study.getName());
            assertContains(e.getMessage(), "complete");
        }
    }

    public void testToSaveStudy() throws Exception {
        command.setStudy(study);
        assertEquals(study, command.toSave());
    }

    public void testToSaveEpoch() throws Exception {
        command.setEpoch(study.getPlannedCalendar().getEpochs().get(1));
        assertEquals(study, command.toSave());
    }

    public void testToSaveArm() throws Exception {
        command.setArm(study.getPlannedCalendar().getEpochs().get(0).getArms().get(1));
        assertEquals(study, command.toSave());
    }

    public void testToSaveAllNull() throws Exception {
        try {
            command.toSave();
            fail("Exception not thrown");
        } catch (IllegalStateException ise) {
            assertEquals("Cannot determine which study the edit was applied to", ise.getMessage());
        }
    }
}
