package edu.northwestern.bioinformatics.studycalendar.testing;

import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
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
