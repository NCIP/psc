package edu.northwestern.bioinformatics.studycalendar.web.admin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationPropertyEditor;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class ConfigurationController extends PscSimpleFormController {
    private Configuration configuration;

    public ConfigurationController() {
        setCommandClass(ConfigurationCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("admin/configure");
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ConfigurationCommand(configuration);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        for (ConfigurationProperty<?> property : configuration.getProperties().getAll()) {
            binder.registerCustomEditor(Object.class, "conf[" + property.getKey() + "].value",
                new ConfigurationPropertyEditor(property));
        }
    }

    @Override
    protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
        return new ModelAndView("redirectToAdministration");
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
