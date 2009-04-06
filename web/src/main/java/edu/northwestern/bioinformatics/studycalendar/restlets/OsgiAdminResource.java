package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.osgi.framework.BundleContext;
import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;
import org.restlet.Context;
import org.restlet.resource.Variant;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * @author Rhett Sutphin
 */
public abstract class OsgiAdminResource extends AbstractPscResource implements ApplicationContextAware {
    private WebApplicationContext applicationContext;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        setAuthorizedFor(Method.GET, Role.SYSTEM_ADMINISTRATOR);
    }

    protected BundleContext getBundleContext() {
        return (BundleContext) applicationContext.getServletContext().getAttribute(
            DaLauncherWebConstants.ServletContextAttributes.BUNDLE_CONTEXT_KEY
        );
    }

    protected WebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (WebApplicationContext) applicationContext;
    }
}
