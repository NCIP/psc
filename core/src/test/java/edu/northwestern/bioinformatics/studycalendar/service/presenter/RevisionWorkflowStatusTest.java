/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;

import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class RevisionWorkflowStatusTest extends StudyCalendarTestCase {
    private Study study;
    private Epoch soleEpoch;
    private Configuration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = assignIds(createInDevelopmentBlankTemplate("[ABC 1234]"));
        // make the baseline workflow-complete
        Add epochAdd = (Add) study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
        soleEpoch = (Epoch) epochAdd.getChild();
        soleEpoch.setName("Treatment");
        soleEpoch.getStudySegments().get(0).setName("Treatment");

        Period p = createPeriod(0, 15, 2);
        p.addPlannedActivity(createPlannedActivity("Sing", 3));
        soleEpoch.getStudySegments().get(0).addPeriod(p);

        assignIds(soleEpoch, 58);
        configuration = registerMockFor(Configuration.class);
    }

    public void testCreationForNotInDevStudyIsAnError() throws Exception {
        study.setDevelopmentAmendment(null);
        try {
            actual();
        } catch (StudyCalendarSystemException scse) {
            assertEquals("Wrong message",
                "Cannot create a RevisionWorkflowStatus instance for a study that is not in development",
                scse.getMessage());
        }
    }

    public void testMessageIncludedForNoEpochs() throws Exception {
        Delta<?> pcDelta = study.getDevelopmentAmendment().getDeltas().get(0);
        pcDelta.removeChange(pcDelta.getChanges().get(0), new Date());
        assertMessages(WorkflowStep.ADD_AT_LEAST_ONE_EPOCH);
    }

    public void testMessageIncludedForStudySegmentWithoutPeriods() throws Exception {
        soleEpoch.getStudySegments().get(0).getPeriods().clear();
        assertMessages(WorkflowStep.STUDY_SEGMENT_NO_PERIODS);
    }

    public void testMessageIncludedForPeriodWithoutActivities() throws Exception {
        soleEpoch.getStudySegments().get(0).getPeriods().first().getPlannedActivities().clear();
        assertMessages(WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES);
    }

    public void testMessagesIncludedForAllNodesSimultaneously() throws Exception {
        soleEpoch.getStudySegments().get(0).getPeriods().first().getPlannedActivities().clear();
        soleEpoch.addStudySegment(setId(93, createNamedInstance("B", StudySegment.class)));
        assertMessages(WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES, WorkflowStep.STUDY_SEGMENT_NO_PERIODS);
    }

    public void testMessageIncludedForUnnamedEpoch() throws Exception {
        soleEpoch.setName(Epoch.TEMPORARY_NAME);
        assertMessages(WorkflowStep.UNNAMED_EPOCH);
    }

    public void testUnnamedEpochMessageIncludedOnlyOnce() throws Exception {
        Delta<?> delta = study.getDevelopmentAmendment().getDeltas().get(0);
        delta.addChanges(
            Add.create(assignIds(Epoch.create(), 40)),
            Add.create(assignIds(Epoch.create(), 50)),
            Add.create(assignIds(Epoch.create(), 60)));
        assertMessages(WorkflowStep.UNNAMED_EPOCH,
            WorkflowStep.STUDY_SEGMENT_NO_PERIODS,
            WorkflowStep.STUDY_SEGMENT_NO_PERIODS,
            WorkflowStep.STUDY_SEGMENT_NO_PERIODS);
    }

    public void testMessageIncludedForUnnamedSegment() throws Exception {
        soleEpoch.getStudySegments().get(0).setName(StudySegment.TEMPORARY_NAME);
        assertMessages(WorkflowStep.UNNAMED_STUDY_SEGMENT);
    }

    public void testUnnamedSegmentMessageIncludedOncePerEpochMax() throws Exception {
        Delta<?> delta = study.getDevelopmentAmendment().getDeltas().get(0);
        delta.addChanges(
            Add.create(assignIds(Epoch.create("Init"), 40)),
            Add.create(assignIds(Epoch.create("Tr", StudySegment.TEMPORARY_NAME, StudySegment.TEMPORARY_NAME), 50)),
            Add.create(assignIds(Epoch.create("FU", StudySegment.TEMPORARY_NAME, StudySegment.TEMPORARY_NAME), 55)));
        assertMessages(
            WorkflowStep.STUDY_SEGMENT_NO_PERIODS,
            WorkflowStep.UNNAMED_STUDY_SEGMENT, WorkflowStep.STUDY_SEGMENT_NO_PERIODS, WorkflowStep.STUDY_SEGMENT_NO_PERIODS,
            WorkflowStep.UNNAMED_STUDY_SEGMENT, WorkflowStep.STUDY_SEGMENT_NO_PERIODS, WorkflowStep.STUDY_SEGMENT_NO_PERIODS);
    }

    public void testIncludesReleaseMessageWhenReleaseable() throws Exception {
        assertMessages(WorkflowStep.RELEASE_REVISION);
    }

    private RevisionWorkflowStatus actual() {
        return new RevisionWorkflowStatus(
            study, AuthorizationObjectFactory.createPscUser("jimbo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER),
            new WorkflowMessageFactory(), getTestingDeltaService(), configuration);
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
