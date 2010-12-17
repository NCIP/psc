package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserActionsResource extends AbstractPscResource {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void init(Context context, Request request, Response response) {
        log.debug("In init: HOORAY!!");
        super.init(context, request, response);
        setAllAuthorizedFor(Method.POST);
        setModifiable(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override public boolean allowPost() { return true; }


    @Override
    public void acceptRepresentation(Representation representation) throws ResourceException {
        log.debug("In acceptRepresentation: " + representation);
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            getResponse().setStatus(Status.SUCCESS_CREATED);

//            getResponse().setLocationRef(String.format(
//                "studies/%s/template", Reference.encode(read.getAssignedIdentifier())));
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }
    }



    @Override
    public Representation represent(Variant variant) throws ResourceException {
            return new JsonRepresentation(new JSONObject());

    }

    private class UserActionJSONRepresentation {
        private JSONObject wrapper;

        private UserActionJSONRepresentation(String json) throws JSONException {
            this.wrapper = new JSONObject(json);
        }
    }
}
