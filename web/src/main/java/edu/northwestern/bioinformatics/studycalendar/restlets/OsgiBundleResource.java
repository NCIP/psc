package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleResource extends OsgiAdminResource {
    private Integer bundleId;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setModifiable(false);
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Representation represent(Variant variant) throws ResourceException {
        String idStr = UriTemplateParameters.BUNDLE_ID.extractFrom(getRequest());
        if (idStr != null) {
            try {
                bundleId = new Integer(idStr);
            } catch (NumberFormatException nfe) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        if (MediaType.APPLICATION_JSON.includes(variant.getMediaType())) {
            return new JsonRepresentation(bundlesJson().toString());
        } else {
            return null;
        }
    }

    private Object bundlesJson() throws ResourceException {
        if (bundleId == null) {
            JSONArray bundles = new JSONArray();
            for (Bundle bundle : getBundles()) {
                bundles.put(toJsonObject(bundle));
            }
            return bundles;
        } else {
            for (Bundle b : getBundles()) {
                if (b.getBundleId() == bundleId) return toJsonObject(b);
            }
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    @SuppressWarnings({"unchecked"})
    private JSONObject toJsonObject(Bundle bundle) {
        JSONObject obj = new JSONObject(new MapBuilder<String, Object>().
            put("id", bundle.getBundleId()).
            put("symbolic_name", bundle.getSymbolicName()).
            put("state", OsgiBundleState.valueOfConstant(bundle.getState()).name()).
            toMap()
        );
        Dictionary<String, Object> headers = bundle.getHeaders();
        Enumeration<String> headerNames = headers.keys();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.startsWith("Bundle-")) {
                String key = name.substring(7).toLowerCase();
                try {
                    obj.put(key, headers.get(name));
                } catch (JSONException e) {
                    log.debug("Unexpected exception in OSGi metadata JSONification", e);
                }
            }
        }
        return obj;
    }

    private Bundle[] getBundles() {
        Bundle[] bundles = getBundleContext().getBundles();
        Arrays.sort(bundles, new Comparator<Bundle>() {
            public int compare(Bundle b1, Bundle b2) {
                return (int) (b1.getBundleId() - b2.getBundleId());
            }
        });
        return bundles;
    }

}
