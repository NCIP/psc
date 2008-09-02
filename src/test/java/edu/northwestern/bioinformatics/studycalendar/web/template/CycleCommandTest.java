package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Jalpa Patel
 */
public class CycleCommandTest extends StudyCalendarTestCase {
    private StudySegment studySegment;
    private CycleCommand command;
    private StudySegmentDao studySegmentDao;

    public void setUp() {
        studySegment = new StudySegment();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);

        command = new CycleCommand(studySegmentDao);
        command.setStudySegment(studySegment);
    }

    public void testApplySetsCycleLength() {
        command.setCycleLength(8);

        command.apply();
        assertEquals("Cycle length not set", (Integer) 8, studySegment.getCycleLength());
    }

    public void testApplySaves() {
        studySegmentDao.save(studySegment);

        replayMocks();
        command.apply();
        verifyMocks();
    }
}
