package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodCountEditor extends PropertyEditorSupport {
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(null);
        } else if (StringUtils.isBlank(text)) {
            setValue(0);
        } else {
            try {
                setValue(new Integer(text.trim()));
            } catch (IllegalArgumentException iae) {
                setValue(1);
            }
        }
    }
}
