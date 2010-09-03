package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Context;
import org.restlet.resource.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.data.*;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Jalpa Patel
 */
public class SourceResource  extends AbstractStorableDomainObjectResource<Source> {
    public SourceService sourceService;
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        addAuthorizationsFor(Method.GET,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                BUSINESS_ADMINISTRATOR,
                DATA_READER);

        addAuthorizationsFor(Method.PUT,
                BUSINESS_ADMINISTRATOR);
    }

    protected Source loadRequestedObject(Request request) throws ResourceException {
        String sourceName = UriTemplateParameters.SOURCE_NAME.extractFrom(request);
        if (sourceName == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No Source name in the request");
        }
        Source source = sourceService.getByName(sourceName);
        if (source == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No Source found with the name " +sourceName);
        }
        return source;
    }


    @Override
    @SuppressWarnings({"unchecked"})
    public void storeRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject entity = new JSONObject(representation.getText());
                Source source = getRequestedObject();
                Boolean manual_flag = (Boolean)entity.get("manual_flag");
                if (manual_flag == true) {
                   store(source);
                } else {
                   throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                           "Manual Target Flag must be true to set source as manual activity target source");
                }
            } catch (JSONException e) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", e);
            } catch (IOException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not read entity", e);
            }
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }
    }

    @Override
    public void store(Source source) throws ResourceException {
        sourceService.makeManualTarget(source);
    }

    @Required
    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
    }
}
