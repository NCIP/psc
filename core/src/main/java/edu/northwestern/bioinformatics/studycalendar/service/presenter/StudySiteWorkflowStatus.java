package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

/**
 * @author Rhett Sutphin
 */
public class StudySiteWorkflowStatus {
    private final StudySite studySite;
    private final PscUser user;
    private final WorkflowMessageFactory workflowMessageFactory;
    private final UserStudySiteRelationship ussr;

    public StudySiteWorkflowStatus(StudySite studySite, PscUser user, WorkflowMessageFactory workflowMessageFactory) {
        this.studySite = studySite;
        this.user = user;
        this.workflowMessageFactory = workflowMessageFactory;

        this.ussr = new UserStudySiteRelationship(user, studySite);
    }

    public WorkflowMessage getMessage() {
        if (studySite.getAmendmentApprovals().isEmpty()) {
            return workflowMessageFactory.createMessage(WorkflowStep.APPROVE_AMENDMENT, ussr);
        }
        return null;
    }

    public StudySite getStudySite() {
        return studySite;
    }

    public PscUser getUser() {
        return user;
    }
}
