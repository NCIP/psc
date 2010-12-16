package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Application;

/**
 * Configures the built-in restlet filters for PSC.
 *
 * @author Rhett Sutphin
 */
public class PscApplication extends Application {
    public PscApplication() {
        super();
        getTunnelService().setExtensionsTunnel(true);
    }
}
