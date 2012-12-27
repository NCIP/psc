/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.WeekdayBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ScheduleServiceTest extends StudyCalendarTestCase {
    private static final String REVISION_DISPLAY_NAME = "10/01/1926 (Leopard)";
    private static final String textValue = "Activity URI Text";

    private ScheduleService service;
    private SubjectService subjectService;
    private TemplateService templateService;
    private ActivityService activityService;

    private ScheduledStudySegment scheduledStudySegment;
    private Site site;
    private Amendment amendment;
    private StudySegmentDao studySegmentDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Just so there's a non-null chain from event to Site
        site = createNamedInstance("The Sun", Site.class);
        Study study = createBasicTemplate();
        StudySubjectAssignment assignment
            = createAssignment(study, site, createSubject("Alice", "Wonder"));
        scheduledStudySegment = new ScheduledStudySegment();
        assignment.getScheduledCalendar().addStudySegment(scheduledStudySegment);

        amendment = createAmendments("Leopard");
        amendment.setDate(DateTools.createDate(1926, Calendar.OCTOBER, 1));

        service = new ScheduleService();
        // this is a real instance instead of mock because eventually
        // some or all of the methods invoked by SS on PS are going to
        // be moved into SS.
        subjectService = new SubjectService();
        service.setSubjectService(subjectService);

        activityService = registerMockFor(ActivityService.class);
        templateService = new TestingTemplateService();
        service.setActivityService(activityService);
        service.setTemplateService(templateService);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        service.setStudySegmentDao(studySegmentDao);
    }

    public void testReviseDateForScheduledScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 1);
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, 7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals("Shifted forward 7 days in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 8, event.getActualDate());
    }

    public void testReviseDateForConditionalScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            ScheduledActivityMode.CONDITIONAL.createStateInstance(DateTools.createDate(2004, Calendar.APRIL, 30), "DC"));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(3, event.getAllStates().size());
        assertEquals("Shifted back 7 days in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 23, event.getActualDate());
    }

    public void testReviseConditionForConditionalScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC 1", 2004, Calendar.APRIL, 30,
            ScheduledActivityMode.CONDITIONAL.createStateInstance(DateTools.createDate(2004, Calendar.APRIL, 30), "DC 2"));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, 0, amendment);
        assertEquals(3, event.getAllStates().size());
        assertEquals("State change in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
        assertEquals("Wrong state of the event", ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
    }

    public void testReviseDateForOccurredScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            ScheduledActivityMode.OCCURRED.createStateInstance(DateTools.createDate(2004, Calendar.APRIL, 30), "DC"));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.OCCURRED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 30, event.getActualDate());
    }

    public void testReviseDateForCanceledScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2004, Calendar.APRIL, 24), "DC"));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.CANCELED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 24, event.getActualDate());
    }

    public void testReviseDateForNotApplicableScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            ScheduledActivityMode.NOT_APPLICABLE.createStateInstance(DateTools.createDate(2004, Calendar.APRIL, 24), "DC"));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.NOT_APPLICABLE, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 24, event.getActualDate());
    }

    public void testReviseDateForScheduledScheduledActivityAvoidsBlackouts() throws Exception {
        WeekdayBlackout noThursdays = new WeekdayBlackout();
        noThursdays.setDayOfTheWeek("Thursday");
        site.getBlackoutDates().add(noThursdays);

        ScheduledActivity event = createScheduledActivity("DC", 2007, Calendar.OCTOBER, 2);
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, 2, amendment);

        assertEquals(ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertDayOfDate(2007, Calendar.OCTOBER, 5, event.getActualDate());
        assertEquals(3, event.getAllStates().size());
    }

    public void testResolveNextScheduledStudySegmentWhenStudySegmentFound() throws Exception {
        NextScheduledStudySegment scheduled = createNextScheduledStudySegment();
        StudySegment existingSegment = setGridId("segment-grid0", new StudySegment());
        existingSegment.setId(1);

        assertNull("StudySegment is newly created",scheduled.getStudySegment().getId());
        expect(studySegmentDao.getByGridId("segment-grid0")).andReturn(existingSegment);

        replayMocks();
        NextScheduledStudySegment actual = service.resolveNextScheduledStudySegment(scheduled);
        verifyMocks();

        assertNotNull("Existing StudySegment is not set", actual.getStudySegment().getId());
        assertSame("StudySegment is not same", existingSegment, actual.getStudySegment());
    }

    public void testResolveNextScheduledStudySegmentWhenNoStudySegmentFound() throws Exception {
        NextScheduledStudySegment scheduled = createNextScheduledStudySegment();
        expect(studySegmentDao.getByGridId("segment-grid0")).andReturn(null);

        replayMocks();
        try {
            service.resolveNextScheduledStudySegment(scheduled);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Segment with grid Identifier segment-grid0 not found.", scve.getMessage());
        }
    }

    public void testGenerateUriForScheduledActivityIdentifier() throws Exception {
        String templateValue = "https://test.com?event={scheduled-activity-identifier}";
        ScheduledActivity sa =  expectCreateScheduledActivityWithUri(templateValue);
        sa.setGridId("EventIdentifier");
        replayMocks();
        Map<String,String> uriMap = service.generateActivityTemplateUri(sa);
        verifyMocks();
        assertEquals("Generated template Uri for activity identifier is not correct"
                , "https://test.com?event=EventIdentifier", uriMap.get(textValue));
    }

    public void testGenerateUriForActivityCode() throws Exception {
        String templateValue = "https://test.com?activityCode={activity-code}";
        ScheduledActivity sa =  expectCreateScheduledActivityWithUri(templateValue);
        replayMocks();
        Map<String,String> uriMap = service.generateActivityTemplateUri(sa);
        verifyMocks();
        assertEquals("Generated template Uri for activity code is not correct"
                , "https://test.com?activityCode=Bone Scan", uriMap.get(textValue));
    }

    public void testGenerateUriForStudyPlanDay() throws Exception {
        String templateValue = "https://test.com?planDay={day-from-study-plan}";
        ScheduledActivity sa =  expectCreateScheduledActivityWithUri(templateValue);
        replayMocks();
        Map<String,String> uriMap = service.generateActivityTemplateUri(sa);
        verifyMocks();
        assertEquals("Generated template Uri for study plan day is not correct"
                , "https://test.com?planDay=3", uriMap.get(textValue));
    }

    public void testGenerateUriForStudySubjectIdentifier() throws Exception {
        String templateValue = "https://test.com?StudySubjectIdentifier={study-subject-identifier}";
        String studySubjectId = "S1S1";
        ScheduledActivity sa =  expectCreateScheduledActivityWithUri(templateValue);
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setStudySubjectId(studySubjectId);
        sa.getScheduledStudySegment().getScheduledCalendar().setAssignment(assignment);
        replayMocks();
        Map<String,String> uriMap = service.generateActivityTemplateUri(sa);
        verifyMocks();
        assertEquals("Generated template Uri for activity identifier is not correct"
                , "https://test.com?StudySubjectIdentifier=S1S1", uriMap.get(textValue));
    }

    public void testGeneratedUriForAssignmentIdentifier() throws Exception {
        String templateValue = "https://test.com?assignment={assignment-identifier}";
        ScheduledActivity sa =  expectCreateScheduledActivityWithUri(templateValue);
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setGridId("assignmentId");
        sa.getScheduledStudySegment().getScheduledCalendar().setAssignment(assignment);
        replayMocks();
        Map<String,String> uriMap = service.generateActivityTemplateUri(sa);
        verifyMocks();
        assertEquals("Generated template Uri for assignment identifier is not correct"
                , "https://test.com?assignment=assignmentId", uriMap.get(textValue));
    }

    public void testGeneratedUriForSubjectIdentifier() throws Exception {
        String templateValue = "https://test.com?subject={subject-identifier}";
        ScheduledActivity sa =  expectCreateScheduledActivityWithUri(templateValue);
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        Subject subject = new Subject();
        subject.setPersonId("personId");
        assignment.setSubject(subject);
        sa.getScheduledStudySegment().getScheduledCalendar().setAssignment(assignment);
        replayMocks();
        Map<String,String> uriMap = service.generateActivityTemplateUri(sa);
        verifyMocks();
        assertEquals("Generated template Uri for subject identifier is not correct"
                , "https://test.com?subject=personId", uriMap.get(textValue));
    }

    //Helper Method
    
    private ScheduledActivity expectCreateScheduledActivityWithUri(String templateValue) {
        Activity activity = createActivity("Bone Scan");
        String namespace = "URI";
        ActivityProperty ap1 = Fixtures.createActivityProperty(namespace, "URI.text", textValue);
        ActivityProperty ap2 = Fixtures.createActivityProperty(namespace, "URI.template", templateValue);
        activity.addProperty(ap1);
        activity.addProperty(ap2);
        PlannedActivity plannedActivity = createPlannedActivity(activity,1);
        Period period = Fixtures.createPeriod(3, 5, 1);
        plannedActivity.setPeriod(period);
        ScheduledActivity scheduledActivity = new ScheduledActivity();
        scheduledActivity.setPlannedActivity(plannedActivity);
        scheduledActivity.setActivity(activity);
        scheduledActivity.setRepetitionNumber(0);
        ScheduledStudySegment scheduledStudySegment = Fixtures.createScheduledStudySegment(new StudySegment());
        scheduledActivity.setScheduledStudySegment(scheduledStudySegment);
        Map<String,List<String>> uriList = new TreeMap<String,List<String>>();
        List<String> values = new ArrayList<String>();
        values.add(textValue);
        values.add(templateValue);
        uriList.put(namespace, values);
        expect(activityService.createActivityUriList(scheduledActivity.getActivity())).andReturn(uriList);
        return scheduledActivity;
    }
    
    private NextScheduledStudySegment createNextScheduledStudySegment() {
        NextScheduledStudySegment scheduledSegment = new NextScheduledStudySegment();
        scheduledSegment.setStartDate(DateTools.createDate(2010, Calendar.APRIL, 24));
        scheduledSegment.setStudySegment(setGridId("segment-grid0", new StudySegment()));
        scheduledSegment.setMode(NextStudySegmentMode.PER_PROTOCOL);
        return scheduledSegment;
    }
}
