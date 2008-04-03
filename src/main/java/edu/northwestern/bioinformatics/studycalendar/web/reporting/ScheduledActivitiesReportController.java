package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRowDao;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
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
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel());
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

    protected Map createModel() {
        Map<String, Object> ref = new HashMap<String, Object>();
        ref.put("modes", ScheduledActivityMode.values());
        return ref;
    }

    ////// Bean Setters
    public void ScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao dao) {
        this.dao = dao;
    }
}
