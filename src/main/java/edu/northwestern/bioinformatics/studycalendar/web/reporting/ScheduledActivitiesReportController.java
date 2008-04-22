package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportController extends AbstractCommandController {
    private ScheduledActivitiesReportRowDao dao;
    private ControllerTools controllerTools;

    public ScheduledActivitiesReportController() {
        setCommandClass(ScheduledActivitiesReportCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ScheduledActivitiesReportCommand(new ScheduledActivitiesReportFilters());
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(ScheduledActivityMode.class, "filters.currentStateMode",
            new ControlledVocabularyEditor(ScheduledActivityMode.class, true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Date.class, "filters.actualActivityDate.start", controllerTools.getDateEditor(false));
        binder.registerCustomEditor(Date.class, "filters.actualActivityDate.stop", controllerTools.getDateEditor(false));
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ScheduledActivitiesReportCommand command = (ScheduledActivitiesReportCommand) oCommand;
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel(errors, search(errors, command)));
    }

    private List<ScheduledActivitiesReportRow> search(Errors errors, ScheduledActivitiesReportCommand command) {
        if (errors.hasErrors()) {
            return Collections.emptyList();
        } else {
            return search(command);
        }
    }

    protected List<ScheduledActivitiesReportRow> search(ScheduledActivitiesReportCommand command) {
        return dao.search(command.getFilters());
    }

    @SuppressWarnings({"unchecked"})
    protected Map createModel(BindException errors, List<ScheduledActivitiesReportRow> results) {
        Map<String, Object> model = errors.getModel();
        model.put("modes", ScheduledActivityMode.values());
        model.put("results", results);
        model.put("resultSize", results.size());
        return model;
    }

    ////// Bean Setters
    public void setScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao dao) {
        this.dao = dao;
    }

    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }
}
