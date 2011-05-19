package edu.northwestern.bioinformatics.studycalendar.restlets;

import gov.nih.nci.cabig.ctms.tools.BuildInfo;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class SystemVersionResource extends AbstractPscResource {
    private BuildInfo buildInfo;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException {
        return new JsonRepresentation(
            new JSONObject(Collections.singletonMap("psc_version", buildInfo.getVersionNumber())));
    }

    ////// CONFIGURATION

    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }
}
