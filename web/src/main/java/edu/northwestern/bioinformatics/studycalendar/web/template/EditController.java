/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.WorkflowService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateActionStatus;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Controller front-end for various asynchronous edits, both on the main template
 * design interface and in manage period.  The meat of a particular edit is handled
 * by an implementor of {@link EditCommand}.
 *
 * @author Rhett Sutphin
 */
public class EditController extends PscAbstractCommandController<EditCommand> {
    private StudyDao studyDao;
    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;
    private ActivityDao activityDao;
    private PopulationDao populationDao;
    private PeriodDao periodDao;

    private String commandBeanName;
    private WorkflowService workflowService;
    private ApplicationSecurityManager applicationSecurityManager;

    public EditController() {
        setCommandClass(EditCommand.class);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return getApplicationContext().getBean(commandBeanName);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
        getControllerTools().registerDomainObjectEditor(binder, "epoch", epochDao);
        getControllerTools().registerDomainObjectEditor(binder, "period", periodDao);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        getControllerTools().registerDomainObjectEditor(binder, "activity", activityDao);
        getControllerTools().registerDomainObjectEditor(binder, "population", populationDao);

        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(false));
        binder.registerCustomEditor(String.class, "details", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "conditionalDetails", new StringTrimmerEditor(true));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
        binder.registerCustomEditor(String.class, "label", new StringTrimmerEditor(true));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ModelAndView handle(EditCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("POST".equals(request.getMethod())) {
            boolean result = command.apply();
            Map<String, Object> model = command.getModel();
            Study study = (Study) model.get("study");
            model.putAll(errors.getModel());

            PscUser user = applicationSecurityManager.getUser() ;
            StudyWorkflowStatus workflow = workflowService.build(study, user);
            model.put("studyWorkflowMessages", workflow.getMessages());

            TemplateActionStatus status = new TemplateActionStatus(workflow, true);
            model.put("templateActions", status.getActions());

            model.put("error", result);
            return new ModelAndView(createViewName(command), model);
        } else {
            // All edits are non-idempotent, so...
            getControllerTools().sendPostOnlyError(response);
            return null;
        }
    }

    private String createViewName(EditCommand command) {
        return "template/ajax/" + command.getRelativeViewName();
    }

    ////// CONFIGURATION

    @Required
    public void setCommandBeanName(String commandBeanName) {
        this.commandBeanName = commandBeanName;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }

    @Required
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
