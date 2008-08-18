package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.ext.servlet.ServletConverter;
import org.restlet.Restlet;
import org.restlet.Context;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Modification of RestletFrameworkServlet to create an Application, if appropriate.
 *
 * @author Rhett Sutphin
 */
public class PscRestletFrameworkServlet extends FrameworkServlet {
    /** The default bean name for the target Restlet. */
    private static final String DEFAULT_TARGET_RESTLET_BEAN_NAME = "root";

    private static final long serialVersionUID = 1L;

    /** The converter of Servlet calls into Restlet equivalents. */
    private volatile ServletConverter converter;

    /** The bean name of the target Restlet. */
    private volatile String targetRestletBeanName;

    @Override
    protected void doService(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        this.converter.service(request, response);
    }

    /**
     * Returns the target Restlet from Spring's Web application context.
     *
     * @return The target Restlet.
     */
    protected Restlet getTargetRestlet() {
        return (Restlet) getWebApplicationContext().getBean(
                getTargetRestletBeanName());
    }

    /**
     * Returns the bean name of the target Restlet. Returns "root" by default.
     *
     * @return The bean name.
     */
    public String getTargetRestletBeanName() {
        return (this.targetRestletBeanName == null) ? DEFAULT_TARGET_RESTLET_BEAN_NAME
                : this.targetRestletBeanName;
    }

    @Override
    protected void initFrameworkServlet() throws ServletException,
            BeansException {
        super.initFrameworkServlet();
        this.converter = new ServletConverter(getServletContext());
        Restlet target = getTargetRestlet();
        if (!(target instanceof org.restlet.Application)) {
            target = new Application();
        }
        this.converter.setTarget(target);
    }

    /**
     * Sets the bean name of the target Restlet.
     *
     * @param targetRestletBeanName
     *            The bean name.
     */
    public void setTargetRestletBeanName(String targetRestletBeanName) {
        this.targetRestletBeanName = targetRestletBeanName;
    }

    protected class Application extends org.restlet.Application {
        public Application() {
            super(new Context());
        }

        @Override
        public Restlet createRoot() {
            return getTargetRestlet();
        }
    }
}
