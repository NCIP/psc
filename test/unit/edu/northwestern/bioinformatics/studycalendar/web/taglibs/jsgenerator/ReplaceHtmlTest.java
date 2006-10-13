package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;


/**
 * @author Rhett Sutphin
 */
public class ReplaceHtmlTest extends JsGeneratorTestCase<ReplaceHtml> {

    protected ReplaceHtml createTag() { return new ReplaceHtml(); }

    public void testStart() throws Exception {
        replayMocks();
        assertEquals(BodyTag.EVAL_BODY_BUFFERED, getTag().doStartTag());
        verifyMocks();
    }

    public void testEnd() throws Exception {
        expectBody("content");
        expectWrite("Element.update(\"" + TARGET_ELEMENT + "\", \"content\")");

        doEndTag();
    }

    public void testEndWithComplexContent() throws Exception {
        expectBody("<div id=\"foo\">\n  <strong>Bad</strong>\n</div>");
        expectWrite("Element.update(\"" + TARGET_ELEMENT + "\", \"<div id=\\\"foo\\\">\\n  <strong>Bad</strong>\\n</div>\")");

        doEndTag();
    }
}
