package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleListResource extends OsgiAdminResource {
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setModifiable(false);
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
            put("state", OsgiBundleState.valueOfConstant(bundle.getState()).name()).
            put("version", bundle.getHeaders().get("Bundle-Version")).
            toMap()
        );
    }

    private Bundle[] getBundles() {
        return getBundleContext().getBundles();
    }

}
