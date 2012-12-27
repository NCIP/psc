/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.WorkflowService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class ReleaseAmendmentController extends PscSimpleFormController implements PscAuthorizedHandler {
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private static final String UNNAMED_EPOCH = "[Unnamed epoch]";
    private WorkflowService workflowService;
    private ApplicationSecurityManager applicationSecurityManager;

    public ReleaseAmendmentController() {
        setCommandClass(ReleaseAmendmentCommand.class);
        setFormView("template/releaseAmendment");
        setSuccessView("redirectToStudyList");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

//Study QA Manager (for lead site)
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_QA_MANAGER);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ReleaseAmendmentCommand(amendmentService);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
         // for breadcrumbs
        ReleaseAmendmentCommand command = (ReleaseAmendmentCommand) oCommand;
        Study theRevisedStudy = deltaService.revise(command.getStudy(), command.getStudy().getDevelopmentAmendment());

        List<Epoch> epochs = theRevisedStudy.getPlannedCalendar().getEpochs();

        ModelMap model = new ModelMap("epochs", epochs);

        StudyWorkflowStatus workflow = workflowService.build(theRevisedStudy, applicationSecurityManager.getUser());
        model.put("studyWorkflowMessages", workflow.getStructureRelatedMessages());

        model.addObject("study", command.getStudy());
        return model;

    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        ((ReleaseAmendmentCommand) oCommand).apply();
        // don't want a model because it's a redirect with no params
        return new ModelAndView(getSuccessView());
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Release");
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            return Collections.singletonMap("study", context.getStudy().getId().toString());
        }
    }
}
