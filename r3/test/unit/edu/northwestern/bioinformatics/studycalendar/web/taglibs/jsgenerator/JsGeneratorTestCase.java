package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.testing.MockPageContext;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.Tag;

import org.easymock.classextension.EasyMock;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public abstract class JsGeneratorTestCase<T extends ElementUpdatingGenerator> extends StudyCalendarTestCase {
    protected static final String TARGET_ELEMENT = "target";
    private JspWriter writer;
    private MockPageContext pageContext;
    private BodyContent bodyContent;

    private T tag;

    protected void setUp() throws Exception {
        super.setUp();

        writer = registerMockFor(JspWriter.class);
        pageContext = new MockPageContext(writer);
        bodyContent = registerMockFor(BodyContent.class);

        tag = createTag();
        tag.setPageContext(pageContext);
        tag.setTargetElement(TARGET_ELEMENT);
        tag.setBodyContent(bodyContent);
    }

    protected abstract T createTag();

    protected T getTag() { return tag; }

    protected void expectBody(String content) {
        EasyMock.expect(bodyContent.getString()).andReturn(content);
    }

    protected void expectWrite(String expected) {
        try {
            writer.write(expected);
        } catch (IOException e) {
            throw new StudyCalendarError("We didn't tell the mock to throw an exception, so it shouldn't", e);
        }
    }

    protected void doEndTag() throws JspException {
        replayMocks();
        assertEquals(Tag.EVAL_PAGE, getTag().doEndTag());
        verifyMocks();
    }
}
