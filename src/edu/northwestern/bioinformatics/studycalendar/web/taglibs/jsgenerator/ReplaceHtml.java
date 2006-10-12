package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class ReplaceHtml extends BodyTagSupport {
    private String targetElement;

    public void setTargetElement(String targetElement) {
        this.targetElement = targetElement;
    }

    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write(call("Element.update", targetElement, getBodyContent().getString()));
        } catch (IOException e) {
            throw new JspException("", e);
        }
        return EVAL_PAGE;
    }

    private String call(String method, Object... parameters) {
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
}
