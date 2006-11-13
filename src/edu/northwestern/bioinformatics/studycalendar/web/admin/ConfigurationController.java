package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.utils.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.utils.configuration.ConfigurationProperty;
import edu.northwestern.bioinformatics.studycalendar.utils.configuration.ConfigurationPropertyEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class ConfigurationController extends PscSimpleFormController {
    private Configuration configuration;

    public ConfigurationController() {
        setCommandClass(ConfigurationCommand.class);
        setFormView("admin/configure");
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ConfigurationCommand(configuration);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        for (ConfigurationProperty property : ConfigurationProperty.values()) {
            binder.registerCustomEditor(Object.class, "conf[" + property.getKey() + "].value",
                new ConfigurationPropertyEditor(property));
        }
    }

    protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
        return new ModelAndView("redirectToStudyList");
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
