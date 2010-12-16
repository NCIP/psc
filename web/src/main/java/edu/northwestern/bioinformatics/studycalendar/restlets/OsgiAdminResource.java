package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.osgi.framework.BundleContext;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public abstract class OsgiAdminResource extends AbstractPscResource {
    private BundleContext bundleContext;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        addAuthorizationsFor(Method.GET, SYSTEM_ADMINISTRATOR);
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    @Required
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
