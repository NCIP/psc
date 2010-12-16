package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;

/**
 * @author Jalpa Patel
 */
public class SourceResource extends AbstractPscResource {
    public SourceService sourceService;

    @Override
    public void doInit() {
        super.doInit();
        getAllowedMethods().remove(Method.GET);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        addAuthorizationsFor(Method.PUT,
                BUSINESS_ADMINISTRATOR);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Representation put(Representation representation, Variant variant) throws ResourceException {
        String sourceName = UriTemplateParameters.SOURCE_NAME.extractFrom(getRequest());
        if (sourceName == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No source name in the request");
        }
        Source source = sourceService.getByName(sourceName);
        if (source == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No source found with the name " +sourceName);
        }
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject entity = new JSONObject(representation.getText());
                Boolean manual_target = (Boolean)entity.get("manual_target");
                if (manual_target == true) {
                   sourceService.makeManualTarget(source);
                } else {
                   throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                           "You may not unset the manual target field.  " +
                                   "To set the manual target to a different source, set it to true on that source.");
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

        return null;
    }

    @Required
    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
    }
}
