/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.OsgiBundleRepresentation;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.MetaTypeService;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Dictionary;
import java.util.Enumeration;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleResource extends OsgiAdminResource {
    private Integer bundleId;
    private OsgiLayerTools osgiLayerTools;

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Representation get(Variant variant) throws ResourceException {
        String idStr = UriTemplateParameters.BUNDLE_ID.extractFrom(getRequest());
        if (idStr != null) {
            try {
                bundleId = new Integer(idStr);
            } catch (NumberFormatException nfe) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        if (MediaType.APPLICATION_JSON.includes(variant.getMediaType())) {
            return bundlesJson();
        } else {
            return null;
        }
    }

    private Representation bundlesJson() throws ResourceException {
        if (bundleId == null) {
            return OsgiBundleRepresentation.create(getBundleContext().getBundles(), getMetaTypeService());
        } else {
            for (Bundle b : getBundleContext().getBundles()) {
                if (b.getBundleId() == bundleId) {
                    return OsgiBundleRepresentation.create(b, getMetaTypeService());
                }
            }
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    private MetaTypeService getMetaTypeService() {
        return osgiLayerTools.getRequiredService(MetaTypeService.class);
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

    ////// CONFIGURATION

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }
}
