/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.osgi.framework.Bundle;
import org.restlet.resource.ResourceException;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public class OsgiSingleBundleResource extends OsgiAdminResource {
    private Bundle bundle;

    protected synchronized Bundle getBundle() throws ResourceException {
        if (bundle == null) {
            String bundleId = UriTemplateParameters.BUNDLE_ID.extractFrom(getRequest());
            try {
                bundle = getBundleContext().getBundle(Long.parseLong(bundleId));
            } catch (NumberFormatException nfe) {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Invalid bundle ID " + bundleId);
            }
            if (bundle == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No bundle with ID " + bundleId);
            }
        }
        return bundle;
    }
}
