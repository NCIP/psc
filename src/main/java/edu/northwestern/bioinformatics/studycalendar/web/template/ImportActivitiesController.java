package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.dataloaders.MultipartFileActivityLoader;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ImportActivitiesController extends PscSimpleFormController {
    private MultipartFileActivityLoader activityLoader;

    public ImportActivitiesController() {
        setCommandClass(ImportActivitiesCommand.class);
        setFormView("template/importActivities");
        setSuccessView("redirectToStudyList");
        setCrumb(new DefaultCrumb());
    }

    protected ModelAndView onSubmit(Object o, BindException errors) throws Exception {
        ImportActivitiesCommand command = (ImportActivitiesCommand) o;
        command.apply();
        if (command.getReturnToPeriodId() == null) {
            Map<String, Object> model = errors.getModel();
            return new ModelAndView(getSuccessView(), model);
        } else {
            ModelMap model = new ModelMap("id", command.getReturnToPeriodId());
            return new ModelAndView("redirectToManagePeriod", model);
        }
    }

    protected ImportActivitiesCommand formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ImportActivitiesCommand command = new ImportActivitiesCommand();
        command.setActivityLoader(activityLoader);
        return command;
    }

    //// Field setters
    public void setActivityLoader(MultipartFileActivityLoader activityLoader) {
        this.activityLoader = activityLoader;
    }
}
