package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class ReplaceHtml extends ElementUpdatingGenerator {
    protected void writeJavascript() throws JspException {
        writeCall("Element.update", getTargetElement(), getBodyContent().getString());
    }
}
