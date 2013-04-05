/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class ReplaceHtml extends ElementUpdatingGenerator {
    protected void writeJavascript() throws JspException {
        String s = "";
        if (getBodyContent()!=null) {
            s = getBodyContent().getString();
        }
        writeCall("Element.update", getTargetElement(), s);
    }
}
