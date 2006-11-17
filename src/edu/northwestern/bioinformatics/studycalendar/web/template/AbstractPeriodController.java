package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.Errors;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public abstract class AbstractPeriodController<C extends PeriodCommand> extends PscSimpleFormController {
    protected AbstractPeriodController(Class<C> commandClass) {
        setFormView("editPeriod");
        setCommandClass(commandClass);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request,binder);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }


    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("durationUnits", Duration.Unit.values());
        return data;
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        C command = (C) oCommand;
        command.apply();
        Study study = command.getArm().getEpoch().getPlannedCalendar().getStudy();
        return ControllerTools.redirectToCalendarTemplate(study.getId(), command.getArm().getId());
    }

}
