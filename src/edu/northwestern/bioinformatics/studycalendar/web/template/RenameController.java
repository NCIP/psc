package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
public class RenameController extends AbstractCommandController {
    private StudyDao studyDao;
    private EpochDao epochDao;
    private ArmDao armDao;

    public RenameController() {
        setCommandClass(RenameCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new RenameCommand(studyDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "arm", armDao);
        ControllerTools.registerDomainObjectEditor(binder, "epoch", epochDao);
        ControllerTools.registerDomainObjectEditor(binder, "study", studyDao);
    }

    protected ModelAndView handle(
        HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors
    ) throws Exception {
        RenameCommand command = (RenameCommand) oCommand;
        command.apply();
        return new ModelAndView("template/ajax/rename", errors.getModel());
    }

    ////// CONFIGURATION

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }

    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }
}
