package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
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
    private Configuration configuration;

    public StudyWorkflowStatus build(Study study) {
        return build(study, applicationSecurityManager.getUser());
    }

    public StudyWorkflowStatus build(Study study, PscUser user) {
        return new StudyWorkflowStatus(study, user, workflowMessageFactory, deltaService, configuration);
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

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
