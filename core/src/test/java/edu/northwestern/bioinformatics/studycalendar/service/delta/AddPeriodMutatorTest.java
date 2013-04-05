/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class AddPeriodMutatorTest extends StudyCalendarTestCase {
    private static final int STUDY_SEGMENT_ID = 11;
    private static final int PERIOD_ID = 83;

    private AddPeriodMutator mutator;

    private Amendment amendment;
    private Add add;

    private StudySegment studySegment;
    private Period period;
    private ScheduledCalendar scheduledCalendar;

    private PeriodDao periodDao;
    private SubjectService subjectService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySegment = setId(STUDY_SEGMENT_ID, createNamedInstance("A1", StudySegment.class));
        period = setId(PERIOD_ID, createPeriod("P1", 4, 17, 8));
        add = Add.create(period);

        amendment = Fixtures.createAmendments("Oops");
        amendment.setDate(DateTools.createDate(1922, Calendar.APRIL, 5));
        amendment.addDelta(Delta.createDeltaFor(studySegment, add));

        scheduledCalendar = new ScheduledCalendar();

        periodDao = registerDaoMockFor(PeriodDao.class);
        subjectService = registerMockFor(SubjectService.class);

        mutator = new AddPeriodMutator(add, periodDao, subjectService);
    }

    public void testAppliesToSchedules() throws Exception {
        assertTrue(mutator.appliesToExistingSchedules());
    }

    public void testApplyWhenNoRelevantScheduledStudySegments() throws Exception {
        scheduledCalendar.addStudySegment(createScheduledStudySegment(createNamedInstance("Some other study segment", StudySegment.class)));

        // expect nothing to happen

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
    
    public void testApplyToOneRelevantScheduledStudySegment() throws Exception {
        scheduledCalendar.addStudySegment(createScheduledStudySegment(createNamedInstance("Some other study segment", StudySegment.class)));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(studySegment));

        subjectService.schedulePeriod(period, amendment, "Period added in amendment 04/05/1922 (Oops)", scheduledCalendar.getScheduledStudySegments().get(1));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testApplyToMultipleRelevantScheduledStudySegments() throws Exception {
        scheduledCalendar.addStudySegment(createScheduledStudySegment(studySegment));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(createNamedInstance("Some other study segment", StudySegment.class)));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(studySegment));

        subjectService.schedulePeriod(period, amendment, "Period added in amendment 04/05/1922 (Oops)", scheduledCalendar.getScheduledStudySegments().get(0));
        subjectService.schedulePeriod(period, amendment, "Period added in amendment 04/05/1922 (Oops)", scheduledCalendar.getScheduledStudySegments().get(2));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
}
