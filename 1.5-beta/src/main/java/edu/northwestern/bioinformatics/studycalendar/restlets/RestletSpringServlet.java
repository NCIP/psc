package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.ext.servlet.ServletConverter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.io.IOException;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.beans.BeansException;
import org.restlet.Restlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public class RestletSpringServlet extends FrameworkServlet {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_TARGET_RESTLET_BEAN_NAME = "router";

    private ServletConverter converter;

    private String targetRestletBeanName = DEFAULT_TARGET_RESTLET_BEAN_NAME;

    @Override
    protected void initFrameworkServlet() throws ServletException, BeansException {
        super.initFrameworkServlet();
        converter = new ServletConverter(getServletContext());
        converter.setTarget(getTargetRestlet());
    }

    protected Restlet getTargetRestlet() {
        return (Restlet) getWebApplicationContext().getBean(getTargetRestletBeanName());
    }

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        converter.service(request, response);
    }

    ////// BOUND PROPERTIES

    public String getTargetRestletBeanName() {
        return targetRestletBeanName;
    }

    public void setTargetRestletBeanName(String targetRestletBeanName) {
        this.targetRestletBeanName = targetRestletBeanName;
    }
}
