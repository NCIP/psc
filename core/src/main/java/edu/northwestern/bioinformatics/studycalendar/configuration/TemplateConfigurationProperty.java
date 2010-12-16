package edu.northwestern.bioinformatics.studycalendar.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import org.restlet.routing.Template;

public class TemplateConfigurationProperty extends DefaultConfigurationProperty<Template> {
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
