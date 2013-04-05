/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.restlets.representations.PscWadlRepresentation;
import freemarker.template.Configuration;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nataliya Shurupova
 */
public class DocResource extends ServerResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Configuration freemarkerConfiguration;
    private static final String WSDL_DOC_XSLT = "/edu/northwestern/bioinformatics/studycalendar/restlets/wadl_documentation.xsl";

    @Override
    public void doInit() {
        super.doInit();
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new ClasspathResourceRepresentation(MediaType.APPLICATION_W3C_SCHEMA, "psc.xsd"));
        getVariants().add(PscWadlRepresentation.create(freemarkerConfiguration, getRequest()));
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (MediaType.TEXT_HTML.includes(variant.getMediaType())) {
            TransformRepresentation transform = new TransformRepresentation(
                PscWadlRepresentation.create(freemarkerConfiguration, getRequest()),
                new ClasspathResourceRepresentation(MediaType.TEXT_XML, WSDL_DOC_XSLT)
            );
            transform.setMediaType(MediaType.TEXT_HTML);
            return transform;
        } else {
            return super.get(variant);
        }

    }

    @Required
    public void setFreemarkerConfiguration(Configuration configuration) {
        this.freemarkerConfiguration = configuration;
    }

}