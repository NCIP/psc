package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleListResource extends AbstractPscResource implements ApplicationContextAware {
    private WebApplicationContext applicationContext;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        setModifiable(false);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        setAuthorizedFor(Method.GET, Role.SYSTEM_ADMINISTRATOR);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (MediaType.APPLICATION_JSON.includes(variant.getMediaType())) {
            return new JsonRepresentation(bundlesJson().toString());
        } else {
            return null;
        }
    }

    private JSONArray bundlesJson() {
        JSONArray bundles = new JSONArray();
        for (Bundle bundle : getBundles()) {
            bundles.put(toJsonObject(bundle));
        }
        return bundles;
    }

    private JSONObject toJsonObject(Bundle bundle) {
        return new JSONObject(new MapBuilder<String, Object>().
            put("id", bundle.getBundleId()).
            put("symbolic-name", bundle.getSymbolicName()).
            put("state", stateString(bundle.getState())).
            put("version", bundle.getHeaders().get("Bundle-Version")).
            toMap()
        );
    }

    private String stateString(int state) {
        switch(state) {
            case Bundle.UNINSTALLED: return "UNINSTALLED";
            case Bundle.INSTALLED:   return "INSTALLED";
            case Bundle.STARTING:    return "STARTING";
            case Bundle.STOPPING:    return "STOPPING";
            case Bundle.ACTIVE:      return "ACTIVE";
            case Bundle.RESOLVED:    return "RESOLVED";
        }
        throw new IllegalArgumentException("Unexpected state 0x" + Integer.toHexString(state));
    }

    private Bundle[] getBundles() {
        BundleContext bundleContext = (BundleContext) applicationContext.getServletContext().getAttribute(
            DaLauncherWebConstants.ServletContextAttributes.BUNDLE_CONTEXT_KEY
        );
        return bundleContext.getBundles();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (WebApplicationContext) applicationContext;
    }
}
