/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import junit.framework.TestCase;

import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class StudySiteWorkflowStatusTest extends StudyCalendarTestCase {
    private StudySite nuB;
    private Configuration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Study b = assignIds(createBasicTemplate("B"));
        nuB = setId(14, b.addSite(setId(54, createSite("NU", "IL486"))));
        nuB.approveAmendment(b.getAmendment(), new Date());

        configuration = registerMockFor(Configuration.class);
    }

    private StudySiteWorkflowStatus actual() {
        return new StudySiteWorkflowStatus(nuB,
            AuthorizationObjectFactory.createPscUser("jo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER),
            new WorkflowMessageFactory(), configuration);
    }

    public void testNoMessagesWhenApproved() throws Exception {
        assertNoMessage();
    }

    public void testApproveMessageWhenNoApprovals() throws Exception {
        nuB.getAmendmentApprovals().clear();
        assertMessage(WorkflowStep.APPROVE_AMENDMENT);
    }

    private void assertNoMessage() {
        assertNull(actual().getMessage());
    }

    private void assertMessage(WorkflowStep expectedStep) {
        WorkflowMessage actual = actual().getMessage();
        assertNotNull("No message", actual);
        assertEquals("Message is for wrong step", expectedStep, actual.getStep());
        assertNotNull("Message HTML is not generatable", actual.getHtml());
        if (actual.getStep().getUriTemplate() != null) {
            assertNotNull("Message link is not generatable", actual.getActionLink());
        }
    }
}
