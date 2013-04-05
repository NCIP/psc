/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Base class for tags that generate prototype.js-based element changes.
 *
 * @author Rhett Sutphin
 */
public abstract class ElementUpdatingGenerator extends BodyTagSupport {
    private String targetElement;

    /**
     * Template method.
     */
    protected abstract void writeJavascript() throws JspException;

    public final int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    public final int doEndTag() throws JspException {
        writeJavascript();
        return EVAL_PAGE;
    }

    protected void writeCall(String method, Object... parameters) throws JspException {
        String js = call(method, parameters);
        try {
            pageContext.getOut().write(js);
        } catch (IOException e) {
            throw new JspException("Exception encountered when writing " + js, e);
        }
    }

    protected String call(String method, Object... parameters) {
        StringBuffer call = new StringBuffer(method).append('(');
        for (Object parameter : parameters) {
            call.append(createJavascriptLiteralRepresentation(parameter))
                .append(", ");
        }
        call.setLength(call.length() - 2);
        call.append(')');
        return call.toString();
    }

    private String createJavascriptLiteralRepresentation(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return jsString((String) value);
        } else {
            return value.toString();
        }
    }

    private static String jsString(String html) {
        return '"' + StringEscapeUtils.escapeJavaScript(html) + '"';
    }

    ////// ATTRIBUTES

    public String getTargetElement() {
        return targetElement;
    }

    public void setTargetElement(String targetElement) {
        this.targetElement = targetElement;
    }

}
