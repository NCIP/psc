package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodCommandTest extends StudyCalendarTestCase {
    private NewPeriodCommand command;
    private AmendmentService amendmentService;

    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);
        command = new NewPeriodCommand(amendmentService);
    }
    
    public void testApply() throws Exception {
        Arm arm = new Arm();
        command.setArm(arm);

        amendmentService.updateDevelopmentAmendment(arm, Add.create(command.getPeriod()));
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
