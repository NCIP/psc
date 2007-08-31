package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class AmendEditController extends AbstractCommandController {
    private static final Logger log = LoggerFactory.getLogger(AmendEditController.class.getName());

    private StudyDao studyDao;
    private EpochDao epochDao;
    private ArmDao armDao;

    private AmendmentDao amendmentDao;
    private DeltaDao deltaDao;
    private ChangeDao changeDao;

    private String commandBeanName;

    public AmendEditController() {
        setCommandClass(AmendEditCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return getApplicationContext().getBean(commandBeanName);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {

        ControllerTools.registerDomainObjectEditor(binder, "arm", armDao);
        ControllerTools.registerDomainObjectEditor(binder, "epoch", epochDao);
        ControllerTools.registerDomainObjectEditor(binder, "study", studyDao);

        ControllerTools.registerDomainObjectEditor(binder, "amendment", amendmentDao);
        ControllerTools.registerDomainObjectEditor(binder, "delta", deltaDao);
        ControllerTools.registerDomainObjectEditor(binder, "change", changeDao);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        if ("POST".equals(request.getMethod())) {
            AmendEditCommand command = (AmendEditCommand) oCommand;
            command.apply(amendmentDao, changeDao, deltaDao);
            Map<String, Object> model = command.getModel();
            model.put("amendment", command.getAmendment());
            model.putAll(errors.getModel());
            return new ModelAndView(createViewName(command), model);
        } else {
            // All edits are non-idempotent, so...
            ControllerTools.sendPostOnlyError(response);
            return null;
        }
    }

    private String createViewName(AmendEditCommand command) {
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

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    @Required
    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }
}
