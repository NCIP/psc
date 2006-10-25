package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AddToController extends AbstractCommandController {
    private StudyDao studyDao;
    private EpochDao epochDao;

    public AddToController() {
        setCommandClass(AddToCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        if (request.getParameter("study") != null) {
            return new AddEpochCommand(studyDao);
        } else if (request.getParameter("epoch") != null) {
            return new AddArmCommand(studyDao);
        } else {
            throw new IllegalArgumentException("No command matches the given parameters");
        }
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "study", studyDao);
        ControllerTools.registerDomainObjectEditor(binder, "epoch", epochDao);
    }

    protected ModelAndView handle(
        HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors
    ) throws Exception {
        AddToCommand command = (AddToCommand) oCommand;
        command.apply();
        Map<String, Object> model = command.getModel();
        model.putAll(errors.getModel());
        return new ModelAndView(createViewName(command), model);
    }

    private String createViewName(AddToCommand command) {
        StringBuffer viewName = new StringBuffer(command.whatAdded());
        viewName.setCharAt(0, Character.toUpperCase(viewName.charAt(0)));
        viewName.insert(0, "template/ajax/add");
        return viewName.toString();
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }
}
