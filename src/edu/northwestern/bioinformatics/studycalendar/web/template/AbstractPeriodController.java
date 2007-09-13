package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class AbstractPeriodController<C extends PeriodCommand> extends PscSimpleFormController {
    private TemplateService templateService;
    private StudyService studyService;

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
        Study study = templateService.findAncestor(command.getArm(), PlannedCalendar.class).getStudy();
        studyService.save(study);
        return getControllerTools().redirectToCalendarTemplate(study.getId(), command.getArm().getId());
    }

    ////// CONFIGURATION

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
