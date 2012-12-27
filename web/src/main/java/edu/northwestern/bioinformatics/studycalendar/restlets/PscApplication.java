/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Application;

/**
 * Configures the built-in restlet filters for PSC.  Replacements for entire instances are
 * configured in restful-api-servlet.xml.
 *
 * @author Rhett Sutphin
 */
public class PscApplication extends Application {
    public PscApplication() {
        super();
        getTunnelService().setExtensionsTunnel(true);
    }
}
