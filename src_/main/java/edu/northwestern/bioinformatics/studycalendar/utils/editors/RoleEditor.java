package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;

public class RoleEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        Role newValue;

        if (text == null || StringUtils.isBlank(text) ) {
            newValue = null;
        } else {
            newValue = Role.getByCode(text);
        }
        setValue(newValue);
    }

    public String getAsText() {
        return getValue() == null ? null : ((Role) getValue()).toString();
    }
}
