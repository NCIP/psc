package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;
import freemarker.template.Configuration;

import java.util.Collections;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class WadlResource extends Resource {
    private Configuration configuration;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new PscWadlRepresentation(configuration, createDataModel()));
    }

    private Map<String, String> createDataModel() {
        return Collections.singletonMap("baseUri", determineBaseUri());
    }

    private String determineBaseUri() {
        return getRequest().getRootRef().toString();
    }

    ////// CONFIGURATION

    @Required
    public void setFreemarkerConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
