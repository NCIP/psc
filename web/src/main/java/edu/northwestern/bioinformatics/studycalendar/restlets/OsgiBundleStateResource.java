/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleStateResource extends OsgiSingleBundleResource {
    private static List<Integer> STOPPED_STATES = Arrays.asList(Bundle.INSTALLED, Bundle.RESOLVED, Bundle.STOPPING);
    private static List<Integer> STARTED_STATES = Arrays.asList(Bundle.ACTIVE, Bundle.STARTING);

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.PUT, SYSTEM_ADMINISTRATOR);
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (MediaType.APPLICATION_JSON.isCompatible(variant.getMediaType())) {
            return representBundleState();
        } else {
            return null;
        }
    }

    @Override
    public Representation put(Representation representation, Variant variant) throws ResourceException {
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject entity = new JSONObject(representation.getText());
                updateBundleState((String) entity.get("state"));
                getResponse().setEntity(representBundleState());
            } catch (JSONException e) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", e);
            } catch (IOException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not read entity", e);
            }
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }

        return null;
    }

    private void updateBundleState(String stateName) throws ResourceException {
        OsgiBundleState requestedState = getRequestedState(stateName);
        try {
            if (requestedState == OsgiBundleState.STARTING) {
                if (STOPPED_STATES.contains(getBundle().getState())) {
                    getBundle().start();
                }
            } else if (requestedState == OsgiBundleState.STOPPING) {
                if (STARTED_STATES.contains(getBundle().getState())) {
                    getBundle().stop();
                }
            } else if (requestedState.constant() != getBundle().getState()) {
                throw new ResourceException(
                    Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Cannot set a bundle to " + stateName);
            }
        } catch (BundleException e) {
            throw new ResourceException(e);
        }
    }

    private OsgiBundleState getRequestedState(String stateName) throws ResourceException {
        try {
            return OsgiBundleState.valueOf(stateName);
        } catch (IllegalArgumentException iae) {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Invalid state " + stateName);
        }
    }

    private JsonRepresentation representBundleState() throws ResourceException {
        JSONObject rep = new JSONObject();
        try {
            rep.put("state", OsgiBundleState.valueOfConstant(getBundle().getState()).name());
        } catch (JSONException e) {
            throw new StudyCalendarSystemException("JSONifying bundle state failed (this shouldn't be possible)", e);
        }
        return new JsonRepresentation(rep);
    }
}
