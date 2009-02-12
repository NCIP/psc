package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodCommandTest extends StudyCalendarTestCase {
    private NewPeriodCommand command;
    private AmendmentService amendmentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);
        command = new NewPeriodCommand(amendmentService);
    }
    
    public void testApply() throws Exception {
        StudySegment studySegment = new StudySegment();
        command.setStudySegment(studySegment);

        amendmentService.updateDevelopmentAmendment(studySegment, Add.create(command.getPeriod()));
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
