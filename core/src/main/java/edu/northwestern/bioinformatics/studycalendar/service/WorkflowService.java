package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowMessageFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class WorkflowService {
    private WorkflowMessageFactory workflowMessageFactory;
    private DeltaService deltaService;
    private ApplicationSecurityManager applicationSecurityManager;

    public StudyWorkflowStatus build(Study study) {
        return new StudyWorkflowStatus(
            study, applicationSecurityManager.getUser(),
            workflowMessageFactory, deltaService);
    }

    ////// CONFIGURATION

    @Required
    public void setWorkflowMessageFactory(WorkflowMessageFactory workflowMessageFactory) {
        this.workflowMessageFactory = workflowMessageFactory;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
