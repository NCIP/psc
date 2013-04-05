/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.util.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;

/**
 * @author Nataliya Shurupova
 */
public class DateCustomEditor extends CustomDateEditor {

    private final DateFormat dateFormat;
    private final boolean allowEmpty;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DateCustomEditor(DateFormat dateFormat, boolean allowEmpty) {
        super(dateFormat, allowEmpty);
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
    }
    
    //overriding the setAsText to parse the date to accept formats like 'mm/dd/yyyy and 'm/d/yyyy'
    public void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            setValue(null);
        }
        else {
            String[] dateParts = text.split("/");
            String year = dateParts[dateParts.length-1];
            if (Integer.parseInt(year) < 1800) {
                throw new IllegalArgumentException("Could not parse year: " + year);
            }
            try {
                setValue(this.dateFormat.parse(text));
            }
            catch (Exception ex) {
                throw new IllegalArgumentException("Could not parse date: " + text);
            }
        }
    }
}
