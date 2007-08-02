package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

/**
 * @author Rhett Sutphin
 */
public class InsertHtmlTest extends JsGeneratorTestCase<InsertHtml> {
    protected InsertHtml createTag() { return new InsertHtml(); }
    
    public void testPositionTop() throws Exception {
        getTag().setPosition("top");
        expectBody("content");
        expectWrite("new Insertion.Top(\"" + TARGET_ELEMENT + "\", \"content\")");

        doEndTag();
    }

    public void testPositionBottom() throws Exception {
        getTag().setPosition("bottom");
        expectBody("content");
        expectWrite("new Insertion.Bottom(\"" + TARGET_ELEMENT + "\", \"content\")");

        doEndTag();
    }

    public void testPositionAfter() throws Exception {
        getTag().setPosition("after");
        expectBody("content");
        expectWrite("new Insertion.After(\"" + TARGET_ELEMENT + "\", \"content\")");

        doEndTag();
    }

    public void testPositionBefore() throws Exception {
        getTag().setPosition("before");
        expectBody("content");
        expectWrite("new Insertion.Before(\"" + TARGET_ELEMENT + "\", \"content\")");

        doEndTag();
    }
}
