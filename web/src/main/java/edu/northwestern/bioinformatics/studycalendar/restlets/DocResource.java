package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.resource.*;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import freemarker.template.Configuration;

/**
 * @author Nataliya Shurupova
 */
public class DocResource extends Resource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Configuration freemarkerConfiguration;
    private static final String WSDL_DOC_XSLT = "/edu/northwestern/bioinformatics/studycalendar/restlets/wadl_documentation.xsl";
    private static final String MIME_APPLICATION_X_XSD_XML = "application/x-xsd+xml";

    @Override
        public void init(Context context, Request request, Response response) {
            super.init(context, request, response);
            getVariants().add(new Variant(MediaType.TEXT_HTML));
            getVariants().add(new ClasspathResourceRepresentation(new MediaType(MIME_APPLICATION_X_XSD_XML), "psc.xsd"));
            getVariants().add(PscWadlRepresentation.create(freemarkerConfiguration, request));
        }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (MediaType.TEXT_HTML.includes(variant.getMediaType())) {
            TransformRepresentation transform = new TransformRepresentation(
                PscWadlRepresentation.create(freemarkerConfiguration, getRequest()),
                new ClasspathResourceRepresentation(MediaType.TEXT_XML, WSDL_DOC_XSLT)
            );
            transform.setMediaType(MediaType.TEXT_HTML);
            return transform;
        } else {
            return super.represent(variant);
        }

    }

    @Required
    public void setFreemarkerConfiguration(Configuration configuration) {
        this.freemarkerConfiguration = configuration;
    }

}