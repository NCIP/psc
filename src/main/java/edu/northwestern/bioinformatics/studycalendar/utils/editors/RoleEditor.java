package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;

public class RoleEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Role newValue;

        if (text == null || StringUtils.isBlank(text) ) {
            newValue = null;
        } else {
            newValue = Role.getByCode(text);
        }
        setValue(newValue);
    }

    @Override
    public String getAsText() {
        return getValue() == null ? null : getValue().toString();
    }
}
