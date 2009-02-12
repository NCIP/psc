package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.restlet.util.Template;

public class TemplateConfigurationProperty extends ConfigurationProperty<Template> {
    public TemplateConfigurationProperty(String propertyName) {
        super(propertyName);
    }

    public String toStorageFormat(Template template) {
        return template.getPattern();
    }

    public Template fromStorageFormat(String s) {
        return new Template(s);
    }
}
