/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;

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

    public void testFillTemplate() throws Exception {
        context.setSite(Fixtures.createSite("Zeppo"));

        Subject subject = new Subject();
        subject.getProperties().add(new SubjectProperty("Expected (Y/N)", "yes"));
        context.setSubject(subject);

        assertEquals("sn=Zeppo expected=yes random= more",
            GeneratedUriTemplateVariable.fillTemplate(
                "sn={site-name} expected={subject-property:Expected (Y/N)} random={subject-property:Not set} more",
                context));
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

    public void testAllUriVariablesHaveDescriptions() throws Exception {
        for (GeneratedUriTemplateVariable variable : GeneratedUriTemplateVariable.values()) {
            assertNotNull("Variable missing description: " + variable, variable.getDescription());
        }
    }
}
