package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class GeneratedUriTemplateVariableTest extends StudyCalendarTestCase {
    private DomainContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = new DomainContext(new TestingTemplateService());
    }

    public void testResolveWhenResolveable() throws Exception {
        String gridId = "Expected";
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setGridId(gridId);
        context.setStudySubjectAssignment(assignment);

        assertEquals("identifier not resolved", gridId, GeneratedUriTemplateVariable.ASSIGNMENT_IDENTIFIER.resolve(context));
    }

    public void testResolveWhenNotResolveable() throws Exception {
        assertNull(GeneratedUriTemplateVariable.ASSIGNMENT_IDENTIFIER.resolve(context));
    }

    public void testResolveStudyIdentifier() throws Exception {
        Study study = new Study();
        study.setAssignedIdentifier("ABC 0532");
        context.setStudy(study);

        assertEquals("Identifier not resolved", "ABC 0532", GeneratedUriTemplateVariable.STUDY_IDENTIFIER.resolve(context));
    }

    public void testResolvePatientIdentifierToGridId() throws Exception {
        Subject subject = new Subject();
        subject.setGridId("24");
        context.setSubject(subject);

        assertEquals("Identifier not resolved", "24", GeneratedUriTemplateVariable.SUBJECT_IDENTIFIER.resolve(context));
    }

    public void testResolvePatientIdentifierToPersonId() throws Exception {
        Subject subject = new Subject();
        subject.setPersonId("36");
        subject.setGridId("25");
        context.setSubject(subject);

        assertEquals("Identifier not resolved", "36", GeneratedUriTemplateVariable.SUBJECT_IDENTIFIER.resolve(context));
    }

    public void testCreateAllVariablesMap() throws Exception {
        String gridId = "Expected";
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setGridId(gridId);
        context.setStudySubjectAssignment(assignment);

        Map<String, Object> all = GeneratedUriTemplateVariable.getAllTemplateValues(context);
        assertEquals("Missing value for assignment ident", gridId, all.get("assignment-identifier"));
    }

    public void testResolveScheduledActivityIdentifier() throws Exception {
        String scheduledActivityIdentifier = "Expected";
        ScheduledActivity scheduledActivity = new ScheduledActivity();
        scheduledActivity.setGridId(scheduledActivityIdentifier);
        context.setScheduledActivity(scheduledActivity);

        assertEquals("identifier not resolved", scheduledActivityIdentifier, GeneratedUriTemplateVariable.SCHEDULED_ACTIVITY_IDENTIFIER.resolve(context));
    }

    public void testResolveActivityCode() throws Exception {
        String activityCode = "activityCode";
        Activity activity  = Fixtures.createActivity(activityCode);
        ScheduledActivity scheduledActivity = new ScheduledActivity();
        scheduledActivity.setActivity(activity);
        context.setScheduledActivity(scheduledActivity);

        assertEquals("Activity code not resolved", activityCode, GeneratedUriTemplateVariable.ACTIVITY_CODE.resolve(context));
    }

    public void testResolveDayFromStudyPlan() throws Exception {
        PlannedActivity plannedActivity = Fixtures.createPlannedActivity(Fixtures.createActivity("New"), 1);
        Period period = Fixtures.createPeriod(3, 5, 1);
        plannedActivity.setPeriod(period);
        ScheduledActivity scheduledActivity = new ScheduledActivity();
        scheduledActivity.setPlannedActivity(plannedActivity);
        scheduledActivity.setRepetitionNumber(0);
        ScheduledStudySegment scheduledStudySegment = Fixtures.createScheduledStudySegment(new StudySegment());
        scheduledActivity.setScheduledStudySegment(scheduledStudySegment);
        context.setScheduledActivity(scheduledActivity);

        assertEquals("Day from study plan not resolved", "3", GeneratedUriTemplateVariable.DAY_FROM_STUDY_PLAN.resolve(context).toString());
    }

    public void testResolveStudySubjectIdentifier() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        String studySubjectIdentifier = "StudySubjectIdentifier";
        assignment.setStudySubjectId(studySubjectIdentifier);
        context.setStudySubjectAssignment(assignment);

        assertEquals("Identifier not resolved", studySubjectIdentifier, GeneratedUriTemplateVariable.STUDY_SUBJECT_IDENTIFIER.resolve(context));
    }

    public void testResolveSiteName() throws Exception {
        Site nu = Fixtures.createSite("Northwestern", "NU312");
        context.setSite(nu);

        assertEquals("Northwestern", GeneratedUriTemplateVariable.SITE_NAME.resolve(context));
    }

    public void testResolveSiteIdentifer() throws Exception {
        Site nu = Fixtures.createSite("Northwestern", "NU312");
        context.setSite(nu);

        assertEquals("NU312", GeneratedUriTemplateVariable.SITE_IDENTIFIER.resolve(context));
    }
}
