package edu.northwestern.bioinformatics.studycalendar.web.template;

import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ReleaseAmendmentCommandTest extends StudyCalendarTestCase {
    private ReleaseAmendmentCommand command;
    private AmendmentService amendmentService;

    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);
        command = new ReleaseAmendmentCommand(amendmentService);

        study = setId(4, new Study());
        study.setPlannedCalendar(new PlannedCalendar());
        command.setStudy(study);
    }

    public void testApply() throws Exception {
        amendmentService.amend(command.getStudy());

        replayMocks();
        command.apply();
        verifyMocks();
    }
}
