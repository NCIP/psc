package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import junit.framework.TestCase;

import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class StudySiteWorkflowStatusTest extends TestCase {
    private StudySite nuB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Study b = assignIds(createBasicTemplate("B"));
        nuB = setId(14, b.addSite(setId(54, createSite("NU", "IL486"))));
        nuB.approveAmendment(b.getAmendment(), new Date());
    }

    private StudySiteWorkflowStatus actual() {
        return new StudySiteWorkflowStatus(nuB,
            AuthorizationObjectFactory.createPscUser("jo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER),
            new WorkflowMessageFactory());
    }

    public void testNoMessagesWhenApproved() throws Exception {
        assertNoMessages();
    }

    public void testApproveMessageWhenNoApprovals() throws Exception {
        nuB.getAmendmentApprovals().clear();
        assertMessages(WorkflowStep.APPROVE_AMENDMENT);
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
}
