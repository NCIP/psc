package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class MarkCompleteCommandTest extends StudyCalendarTestCase {
    private MarkCompleteCommand command;
    private StudyDao studyDao;

    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        command = new MarkCompleteCommand(studyDao);

        study = setId(4, new Study());
        study.setPlannedCalendar(new PlannedCalendar());
        command.setStudy(study);
    }

    public void testApplyTrue() throws Exception {
        study.getPlannedCalendar().setComplete(false);
        command.setCompleted(true);
        studyDao.save(command.getStudy());

        replayMocks();
        command.apply();
        verifyMocks();

        assertTrue(study.getPlannedCalendar().isComplete());
    }

    public void testApplyFalse() throws Exception {
        study.getPlannedCalendar().setComplete(true);
        command.setCompleted(false);
        studyDao.save(command.getStudy());

        replayMocks();
        command.apply();
        verifyMocks();

        assertFalse(study.getPlannedCalendar().isComplete());
    }
}
