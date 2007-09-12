package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;

/**
 * @author Rhett Sutphin
 */
public class EditCommandTest extends StudyCalendarTestCase {
    private EditCommand command;
    private StudyService studyService;

    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);

        command = registerMockFor(EditCommand.class,
            EditCommand.class.getDeclaredMethod("performEdit"),
            EditCommand.class.getDeclaredMethod("getRelativeViewName")
        );
        command.setStudyService(studyService);
        command.setDeltaService(Fixtures.getTestingDeltaService());

        study = Fixtures.createSingleEpochStudy("Study 1234", "E1", "A", "B");
        study.getPlannedCalendar().addEpoch(Epoch.create("E2"));
        study.setDevelopmentAmendment(new Amendment());
    }

    public void testApply() throws Exception {
        command.setStudy(study);
        command.performEdit();
        studyService.save(study);

        replayMocks();
        command.apply();
        verifyMocks();
    }
    
    public void testApplyToCompleteCalendar() throws Exception {
        study.setDevelopmentAmendment(null);

        try {
            command.setStudy(study);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertContains(e.getMessage(), study.getName());
            assertContains(e.getMessage(), "not in development");
        }
    }

    /* TODO: don't expect to need this anymore.  Keep until sure.
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
    } */
}
