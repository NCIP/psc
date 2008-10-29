package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class EditTemplateCommandTest extends StudyCalendarTestCase {
    private EditTemplateCommand command;
    private StudyService studyService;

    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);

        command = registerMockFor(EditTemplateCommand.class,
            EditTemplateCommand.class.getMethod("validAction"),
            EditTemplateCommand.class.getMethod("performEdit"),
            EditTemplateCommand.class.getMethod("getRelativeViewName")
        );
        command.setStudyService(studyService);
        command.setDeltaService(Fixtures.getTestingDeltaService());

        study = Fixtures.createSingleEpochStudy("Study 1234", "E1", "A", "B");
        study.getPlannedCalendar().addEpoch(Epoch.create("E2"));
        study.setDevelopmentAmendment(new Amendment());
    }

    public void testApply() throws Exception {
        command.setStudy(study);
        expect(command.validAction()).andReturn(true);
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
}
