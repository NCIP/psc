package edu.northwestern.bioinformatics.studycalendar.testing;

import javax.servlet.jsp.JspWriter;

/**
 * Just like {@link org.springframework.mock.web.MockPageContext}, except it responds to
 * {@link #getOut()} with an externally-configured {@link JspWriter}.
 *
 * @author Rhett Sutphin
 */
public class MockPageContext extends org.springframework.mock.web.MockPageContext {
    private JspWriter writer;

    public MockPageContext(JspWriter writer) {
        super();
        this.writer = writer;
    }

    public JspWriter getOut() {
        return writer;
    }
}
