package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;

/**
 * @author Rhett Sutphin
 */
public class InsertHtml extends ElementUpdatingGenerator {
    private String position;

    protected void writeJavascript() throws JspException {
        String insertion = StringUtils.capitalize(position.toLowerCase());
        writeCall("new Insertion." + insertion, getTargetElement(), getBodyContent().getString());
    }

    ////// ATTRIBUTES

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
