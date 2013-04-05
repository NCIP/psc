/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createPeriod;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createAmendments;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import static org.easymock.EasyMock.expect;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class AddPlannedActivityMutatorTest extends StudyCalendarTestCase {
    private static final int PLANNED_ACTIVITY_ID = 21;

    private AddPlannedActivityMutator mutator;

    private Amendment amendment;
    private Add add;

    private StudySegment studySegment;
    private Period period;
    private PlannedActivity plannedActivity;
    private ScheduledCalendar scheduledCalendar;

    private PlannedActivityDao plannedActivityDao;
    private SubjectService subjectService;
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySegment = setId(45, new StudySegment());
        period = setId(81, createPeriod("P1", 4, 17, 8));

        plannedActivity = setId(21, Fixtures.createPlannedActivity("Swim", 8));
        add = Add.create(plannedActivity, 4);
        amendment = createAmendments("Oops");
        amendment.setDate(DateTools.createDate(1922, Calendar.APRIL, 5));
        amendment.addDelta(Delta.createDeltaFor(period, add));

        scheduledCalendar = new ScheduledCalendar();

        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        subjectService = registerMockFor(SubjectService.class);
        templateService = registerMockFor(TemplateService.class);

        mutator = new AddPlannedActivityMutator(
            add, plannedActivityDao, subjectService, templateService);

        expect(templateService.findParent(period)).andReturn(studySegment);
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

        subjectService.schedulePlannedActivity(plannedActivity, period, amendment, "Activity added in amendment 04/05/1922 (Oops)", scheduledCalendar.getScheduledStudySegments().get(1));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testApplyToMultipleRelevantScheduledStudySegments() throws Exception {
        scheduledCalendar.addStudySegment(createScheduledStudySegment(studySegment));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(createNamedInstance("Some other study segment", StudySegment.class)));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(studySegment));

        subjectService.schedulePlannedActivity(plannedActivity, period, amendment, "Activity added in amendment 04/05/1922 (Oops)", scheduledCalendar.getScheduledStudySegments().get(0));
        subjectService.schedulePlannedActivity(plannedActivity, period, amendment, "Activity added in amendment 04/05/1922 (Oops)", scheduledCalendar.getScheduledStudySegments().get(2));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
}

