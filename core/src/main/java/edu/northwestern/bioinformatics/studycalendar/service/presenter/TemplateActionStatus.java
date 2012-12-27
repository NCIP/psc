/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import java.util.ArrayList;
import java.util.List;

public class TemplateActionStatus {
    private StudyWorkflowStatus studyWorkflowStatus;
    private boolean developmentRevision;

    public TemplateActionStatus(StudyWorkflowStatus studyWorkflowStatus, boolean developmentRevision) {
        this.studyWorkflowStatus = studyWorkflowStatus;
        this.developmentRevision = developmentRevision;
    }

    public List<TemplateAction> getActions() {
        List<TemplateAction> actions = new ArrayList<TemplateAction>();
        
        if (developmentRevision) {
            WorkflowMessage studyMessage = studyWorkflowStatus.getMessagesIgnoringRevisionMessages();
            if ((studyMessage == null || !studyMessage.getStep().equals(WorkflowStep.SET_ASSIGNED_IDENTIFIER)) && studyWorkflowStatus.isRevisionComplete()) {
                actions.add(TemplateAction.RELEASE_REVISION);
            }
        }

        return actions;
    }


}
