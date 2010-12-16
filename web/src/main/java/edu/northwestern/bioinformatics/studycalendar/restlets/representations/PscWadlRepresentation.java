package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import freemarker.template.Configuration;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;

import java.util.Map;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class PscWadlRepresentation extends TemplateRepresentation {
    public static final String PSC_WADL_PATH = "psc.wadl";

    protected PscWadlRepresentation(Configuration config, Object dataModel) {
        super(PSC_WADL_PATH, config, dataModel, MediaType.APPLICATION_WADL_XML);
    }

    public static Representation create(Configuration configuration, Request request) {
        return new PscWadlRepresentation(configuration, createDataModel(request));
    }

    private static Map<String, String> createDataModel(Request request) {
        return Collections.singletonMap("baseUri", determineBaseUri(request));
    }

    private static String determineBaseUri(Request request) {
        return request.getRootRef().toString();
    }

}
