/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Saurabh Agrawal
 */
public class SiteResource extends AbstractRemovableStorableDomainObjectResource<Site> {
    private SiteService siteService;
    private String assignedIdentifier;

    @Override
    public void doInit() {
        super.doInit();

        Site site = getRequestedObjectDuringInit();

        addAuthorizationsFor(Method.PUT, ResourceAuthorization.create(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER, site));
        addAuthorizationsFor(Method.DELETE, ResourceAuthorization.create(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER, site));
        addAuthorizationsFor(Method.GET, site, PscRole.valuesWithSiteScoped());
    }

    @Override
    protected Site loadRequestedObject(Request request) {
        assignedIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);
        if (assignedIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No site identifier in the request");
        }
        Site site = siteService.getByAssignedIdentifier(assignedIdentifier);
        return site;
    }

    @Override
    public void remove(final Site site){
        siteService.removeSite(site);
    }

    @Override
    public Site store(final Site site) {
        siteService.createOrMergeSites(getRequestedObject(), site);
        // TODO: returning the result of the above call would probably make more sense
        return site;
    }

    @Override
    public void verifyRemovable(final Site site) throws ResourceException {
        super.verifyRemovable(site);
        if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"Unknown site with identifier "+ assignedIdentifier);
        }
        if (site.hasAssignments()) {
            String message = "Can not delete the site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()) +
                    " because site has some assignments";
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }
}