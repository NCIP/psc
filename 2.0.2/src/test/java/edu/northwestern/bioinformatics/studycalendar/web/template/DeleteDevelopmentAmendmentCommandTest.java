package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Rhett Sutphin
 */
public class DeleteDevelopmentAmendmentCommandTest extends StudyCalendarTestCase {
    private DeleteDevelopmentAmendmentCommand command;

    private Study study;
    private AmendmentService amendmentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = new Study();
        study.setDevelopmentAmendment(new Amendment());

        amendmentService = registerMockFor(AmendmentService.class);

        command = new DeleteDevelopmentAmendmentCommand(amendmentService);
        command.setStudy(study);
    }
    
    public void testApply() throws Exception {
        amendmentService.deleteDevelopmentAmendment(study);
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
