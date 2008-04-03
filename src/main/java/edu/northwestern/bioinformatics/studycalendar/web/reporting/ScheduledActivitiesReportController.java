package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportController extends AbstractCommandController {
    private ScheduledActivitiesReportRowDao dao;

    public ScheduledActivitiesReportController() {
        setCommandClass(ScheduledActivitiesReportCommand.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel(errors, search(errors)));
    }

    private List<ScheduledActivitiesReportRow> search(Errors errors) {
        if (errors.hasErrors()) {
            return Collections.emptyList();
        } else {
            return search();
        }
    }

    protected List<ScheduledActivitiesReportRow> search() {
        return dao.search();
    }

    protected Map createModel(BindException errors, List<ScheduledActivitiesReportRow> results) {
        Map<String, Object> model = errors.getModel();
        model.put("modes", ScheduledActivityMode.values());
        model.put("results", results);
        return model;
    }

    ////// Bean Setters
    public void setScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao dao) {
        this.dao = dao;
    }
}
