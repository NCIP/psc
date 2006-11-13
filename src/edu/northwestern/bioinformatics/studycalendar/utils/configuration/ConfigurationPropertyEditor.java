package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import java.beans.PropertyEditorSupport;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationPropertyEditor extends PropertyEditorSupport {
    private ConfigurationProperty property;

    public ConfigurationPropertyEditor(ConfigurationProperty property) {
        this.property = property;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(property.fromStorageFormat(text));
    }

    public String getAsText() {
        Object v = getValue();
        return v == null ? null : property.toStorageFormat(v);
    }
}
