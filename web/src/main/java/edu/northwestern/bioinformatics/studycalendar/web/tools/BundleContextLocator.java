package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;

import javax.servlet.ServletContext;

/**
 * {@link FactoryBean} which makes the DA-Launcher-configured OSGi {@link BundleContext}
 * available for Spring wiring.
 *
 * @author Rhett Sutphin
 */
public class BundleContextLocator implements FactoryBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    public Object getObject() throws Exception {
        if (!(applicationContext instanceof WebApplicationContext)) {
            throw new StudyCalendarSystemException("Cannot extract bundle context from normal application context.  (It's in the ServletContext.)");
        }
        ServletContext servletContext = ((WebApplicationContext) applicationContext).getServletContext();
        return servletContext.getAttribute(DaLauncherWebConstants.ServletContextAttributes.BUNDLE_CONTEXT_KEY);
    }

    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Class getObjectType() {
        return BundleContext.class;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
