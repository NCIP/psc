package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class StudyWorkflowStatus {
    private final Study study;
    private final PscUser user;

    private final WorkflowMessageFactory workflowMessageFactory;

    private final UserTemplateRelationship utr;
    private final RevisionWorkflowStatus revisionWorkflowStatus;

    public StudyWorkflowStatus(
        Study study, PscUser user,
        WorkflowMessageFactory factory, DeltaService deltaService
    ) {
        this.study = study;
        this.user = user;
        this.workflowMessageFactory = factory;

        this.utr = new UserTemplateRelationship(user, study);
        if (study.isInDevelopment()) {
            this.revisionWorkflowStatus = new RevisionWorkflowStatus(study, user, factory, deltaService);
        } else {
            this.revisionWorkflowStatus = null;
        }
    }

    public List<WorkflowMessage> getMessages() {
        List<WorkflowMessage> messages = new ArrayList<WorkflowMessage>();
        if (study.getHasTemporaryAssignedIdentifier()) {
            messages.add(workflowMessageFactory.createMessage(WorkflowStep.SET_ASSIGNED_IDENTIFIER, utr));
        }
        if (!study.isReleased()) {
            messages.add(workflowMessageFactory.createMessage(WorkflowStep.COMPLETE_AND_RELEASE_INITIAL_TEMPLATE, utr));
        }
        if (study.isReleased() && study.getStudySites().isEmpty()) {
            messages.add(workflowMessageFactory.createMessage(WorkflowStep.ASSIGN_SITE, utr));
        }
        return messages;
    }

    public Collection<TemplateAvailability> getTemplateAvailabilities() {
        Set<TemplateAvailability> availabilities = new LinkedHashSet<TemplateAvailability>();
        if (getRevisionWorkflowStatus() != null) {
            availabilities.add(TemplateAvailability.IN_DEVELOPMENT);
        }
        if (study.isReleased()) {
            if (!getMessages().isEmpty()) {
                availabilities.add(TemplateAvailability.PENDING);
            }
            for (StudySiteWorkflowStatus status : getStudySiteWorkflowStatuses()) {
                if (status.getMessages().isEmpty()) {
                    availabilities.add(TemplateAvailability.AVAILABLE);
                } else {
                    availabilities.add(TemplateAvailability.PENDING);
                }
            }
        }
        return availabilities;
    }

    /**
     * Returns the development revision workflow for this study, or null if the
     * study isn't in development.
     * @return
     */
    public RevisionWorkflowStatus getRevisionWorkflowStatus() {
        return revisionWorkflowStatus;
    }

    /**
     * Returns the study site workflows for this study.  If the study doesn't have any
     * associated sites, it returns an empty list.
     * @return
     */
    public List<StudySiteWorkflowStatus> getStudySiteWorkflowStatuses() {
        List<StudySiteWorkflowStatus> statuses = new LinkedList<StudySiteWorkflowStatus>();
        for (StudySite ss : study.getStudySites()) {
            statuses.add(new StudySiteWorkflowStatus(ss, getUser(), workflowMessageFactory));
        }
        return statuses;
    }

    public PscUser getUser() {
        return user;
    }

    public Study getStudy() {
        return study;
    }
}
