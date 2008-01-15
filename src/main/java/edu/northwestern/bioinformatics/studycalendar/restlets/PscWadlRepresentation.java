package edu.northwestern.bioinformatics.studycalendar.restlets;

import freemarker.template.Configuration;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;

/**
 * @author Rhett Sutphin
 */
public class PscWadlRepresentation extends TemplateRepresentation {
    public static final String PSC_WADL_PATH = "restlets/psc.wadl";

    public PscWadlRepresentation(Configuration config, Object dataModel) {
        super(PSC_WADL_PATH, config, dataModel, MediaType.APPLICATION_WADL_XML);
    }
}
