package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class RevisionWorkflowStatus {
    private final Study revisedStudy;
    private final PscUser user;
    private final WorkflowMessageFactory workflowMessageFactory;
    private UserTemplateRelationship utr;

    public RevisionWorkflowStatus(Study study, PscUser user, WorkflowMessageFactory workflowMessageFactory, DeltaService deltaService) {
        if (!study.isInDevelopment()) {
            throw new StudyCalendarSystemException(
                "Cannot create a %s instance for a study that is not in development",
                getClass().getSimpleName());
        }
        this.revisedStudy = deltaService.revise(study, study.getDevelopmentAmendment());
        this.user = user;
        this.workflowMessageFactory = workflowMessageFactory;
        this.utr = new UserTemplateRelationship(user, study);
    }

    public List<WorkflowMessage> getMessages() {
        List<WorkflowMessage> messages = new LinkedList<WorkflowMessage>();
        messages.addAll(getRevisionCompletionMessages());

        if (messages.isEmpty()) {
            messages.add(workflowMessageFactory.createMessage(WorkflowStep.RELEASE_REVISION, utr));
        }

        return messages;
    }

    public List<WorkflowMessage> getRevisionCompletionMessages() {
        List<WorkflowMessage> messages = new LinkedList<WorkflowMessage>();

        if (revisedStudy.getPlannedCalendar().getEpochs().isEmpty()) {
            messages.add(workflowMessageFactory.createMessage(WorkflowStep.ADD_AT_LEAST_ONE_EPOCH, utr));
        } else {
            boolean unnamedMessageAdded = false;
            for (Epoch epoch : revisedStudy.getPlannedCalendar().getEpochs()) {
                if (!unnamedMessageAdded && epoch.getHasTemporaryName()) {
                    messages.add(workflowMessageFactory.createMessage(WorkflowStep.UNNAMED_EPOCH, utr));
                    unnamedMessageAdded = true;
                }
                appendMessages(epoch, messages);
            }
        }

        return messages;
    }

    private void appendMessages(Epoch epoch, List<WorkflowMessage> messages) {
        boolean unnamedMessageAdded = false;
        for (StudySegment segment : epoch.getStudySegments()) {
            if (!unnamedMessageAdded && segment.getHasTemporaryName()) {
                messages.add(workflowMessageFactory.createMessage(
                    WorkflowStep.UNNAMED_STUDY_SEGMENT, utr, segment));
                unnamedMessageAdded = true;
            }
            if (segment.getPeriods().isEmpty()) {
                messages.add(workflowMessageFactory.createMessage(
                    WorkflowStep.STUDY_SEGMENT_NO_PERIODS, utr, segment));
            } else {
                for (Period period : segment.getPeriods()) {
                    if (period.getChildren().isEmpty()) {
                        messages.add(workflowMessageFactory.createMessage(
                            WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES, utr, period));
                    }
                }
            }
        }
    }

    public PscUser getUser() {
        return user;
    }

    public Study getRevisedStudy() {
        return revisedStudy;
    }
}
