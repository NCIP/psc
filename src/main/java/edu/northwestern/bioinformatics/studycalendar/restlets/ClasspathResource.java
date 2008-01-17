package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.resource.Resource;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.Context;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class ClasspathResource extends Resource {
    private MediaType mediaType;
    private String resourcePath;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new ClasspathResourceRepresentation(mediaType, resourcePath));
    }

    @Required
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Required
    public void setMimeType(String mimeType) {
        mediaType = MediaType.valueOf(mimeType);
    }
}
