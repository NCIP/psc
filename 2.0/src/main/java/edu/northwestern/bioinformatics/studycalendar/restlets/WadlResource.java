package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.springframework.beans.factory.annotation.Required;
import freemarker.template.Configuration;

/**
 * @author Rhett Sutphin
 */
public class WadlResource extends Resource {
    private Configuration freemarkerConfiguration;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(PscWadlRepresentation.create(freemarkerConfiguration, request));
    }

    ////// CONFIGURATION

    @Required
    public void setFreemarkerConfiguration(Configuration configuration) {
        this.freemarkerConfiguration = configuration;
    }
}
