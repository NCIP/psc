package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class EditController extends PscAbstractCommandController<EditCommand> {
    private StudyDao studyDao;
    private EpochDao epochDao;
    private ArmDao armDao;

    private String commandBeanName;

    public EditController() {
        setCommandClass(EditCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return getApplicationContext().getBean(commandBeanName);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "arm", armDao);
        getControllerTools().registerDomainObjectEditor(binder, "epoch", epochDao);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
    }

    protected ModelAndView handle(EditCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("POST".equals(request.getMethod())) {
            command.apply();
            Map<String, Object> model = command.getModel();
            model.putAll(errors.getModel());
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
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }
}
