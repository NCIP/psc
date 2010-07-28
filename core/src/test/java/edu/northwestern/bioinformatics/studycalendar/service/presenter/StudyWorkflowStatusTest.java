package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class StudyWorkflowStatusTest extends TestCase {
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        study = assignIds(createBasicTemplate("CRM 114"));
        for (Epoch e : study.getPlannedCalendar().getEpochs()) {
            for (StudySegment segment : e.getStudySegments()) {
                Period p = createPeriod(1, 1, 1);
                p.addPlannedActivity(createPlannedActivity("T", 1));
                segment.addPeriod(p);
            }
        }
        StudySite ss = study.addSite(createSite("NU", "IL675"));
        ss.approveAmendment(study.getAmendment(), new Date());
    }

    private StudyWorkflowStatus actual() {
        return new StudyWorkflowStatus(study,
            AuthorizationObjectFactory.createPscUser("jo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER),
            new WorkflowMessageFactory(),
            Fixtures.getTestingDeltaService());
    }

    ////// messages

    public void testIncludesIdentifierMessageWhenHasTemporaryIdentifier() throws Exception {
        study.setAssignedIdentifier("[ABC 1450]");
        assertMessages(WorkflowStep.SET_ASSIGNED_IDENTIFIER);
    }

    public void testDoesNotIncludeIdentifierMessageWhenDoesNotHaveTemporaryIdentifier() throws Exception {
        study.setAssignedIdentifier("ABC 1450");
        assertNoMessages();
    }

    public void testIncludesAssignSitesMessageWhenNoSites() throws Exception {
        study.getStudySites().clear();
        assertMessages(WorkflowStep.ASSIGN_SITE);
    }

    public void testDoesNotIncludeAssignSitesMessageWhenNoSitesButNotReleased() throws Exception {
        study.getStudySites().clear();
        study.setAmendment(null);
        assertMessages(WorkflowStep.COMPLETE_AND_RELEASE_INITIAL_TEMPLATE);
    }

    public void testIncludesReleaseMessageWhenNotReleased() throws Exception {
        study.setAmendment(null);
        assertMessages(WorkflowStep.COMPLETE_AND_RELEASE_INITIAL_TEMPLATE);
    }

    public void testDoesNotIncludeReleaseMessageWhenReleasedAtLeastOnce() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        assertNoMessages();
    }

    private void assertNoMessages() {
        assertMessages();
    }

    private void assertMessages(WorkflowStep... expectedSteps) {
        List<WorkflowMessage> actualMessages = actual().getMessages();
        assertEquals("Wrong number of messages: " + actualMessages, expectedSteps.length, actualMessages.size());
        for (int i = 0; i < expectedSteps.length; i++) {
            WorkflowMessage actual = actualMessages.get(i);
            assertEquals("Message " + i + " is for wrong step", expectedSteps[i], actual.getStep());
            assertNotNull("Message HTML is not generatable", actual.getHtml());
            if (actual.getStep().getUriTemplate() != null) {
                assertNotNull("Message link is not generatable", actual.getActionLink());
            }
        }
    }

    ////// related instances

    public void testRevisionWorkflowIsNullWhenNotInDevelopment() throws Exception {
        assertNull(actual().getRevisionWorkflowStatus());
    }

    public void testRevisionWorkflowWhenInDevelopment() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        RevisionWorkflowStatus actual = actual().getRevisionWorkflowStatus();
        assertNotNull("Should be present", actual);
        assertEquals("For wrong study",
            study.getAssignedIdentifier(), actual.getRevisedStudy().getAssignedIdentifier());
    }

    public void testStudySiteWorkflowsEmptyWhenNoStudySites() throws Exception {
        study.getStudySites().clear();
        List<StudySiteWorkflowStatus> actual = actual().getStudySiteWorkflowStatuses();
        assertNotNull("Should be a collection", actual);
        assertTrue("But an empty one", actual.isEmpty());
    }

    public void testStudySiteWorkflowsHasOnePerStudySite() throws Exception {
        study.addSite(createSite("mayo", "MN567"));
        List<StudySiteWorkflowStatus> actual = actual().getStudySiteWorkflowStatuses();
        assertEquals("Wrong number of statuses", 2, actual.size());
        assertEquals("1st status is for wrong study site", "IL675",
            actual.get(0).getStudySite().getSite().getAssignedIdentifier());
        assertEquals("2nd status is for wrong study site", "MN567",
            actual.get(1).getStudySite().getSite().getAssignedIdentifier());
    }

    /////// availability
    
    public void testIsDevelopmentWhenInDevelopment() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        assertAvailabilityPresent(TemplateAvailability.IN_DEVELOPMENT);
    }

    public void testIsNotDevelopmentWhenNotInDevelopment() throws Exception {
        study.setDevelopmentAmendment(null);
        assertAvailabilityNotPresent(TemplateAvailability.IN_DEVELOPMENT);
    }

    public void testIsPendingWhenReleasedAndHasStudyMessages() throws Exception {
        study.getStudySites().clear();
        assertAvailabilityPresent(TemplateAvailability.PENDING);
    }

    public void testIsNotAvailableWhenNotReleased() throws Exception {
        study.setAmendment(null);
        assertAvailabilityNotPresent(TemplateAvailability.AVAILABLE);
    }

    public void testNotAvailableWhenReleasedButHasStudyMessages() throws Exception {
        study.getStudySites().clear();
        assertAvailabilityNotPresent(TemplateAvailability.AVAILABLE);
    }

    public void testBothAvailableAndPendingWhenOneStudySiteIsReadyAndAnotherIsNot() throws Exception {
        setId(81, study.addSite(setId(108, createSite("mayo", "MN459"))));
        assertAvailabilityPresent(TemplateAvailability.AVAILABLE);
        assertAvailabilityPresent(TemplateAvailability.PENDING);
    }

    private void assertAvailabilityPresent(TemplateAvailability availability) {
        Collection<TemplateAvailability> actual = actual().getTemplateAvailabilities();
        assertTrue("Missing expected availability " + availability + "; present: " + actual,
            actual.contains(availability));
    }

    private void assertAvailabilityNotPresent(TemplateAvailability availability) {
        Collection<TemplateAvailability> actual = actual().getTemplateAvailabilities();
        assertFalse("Unexpected availability " + availability + "; present: " + actual,
            actual.contains(availability));
    }
}
