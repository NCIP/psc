package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Saurabh Agrawal
 */
public class SiteResource extends AbstractRemovableStorableDomainObjectResource<Site> {

    private SiteService siteService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.SYSTEM_ADMINISTRATOR);
        setAuthorizedFor(Method.DELETE, Role.SYSTEM_ADMINISTRATOR);
    }


    @Override
    protected Site loadRequestedObject(Request request) {
        String assignedIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);
        return siteService.getByAssignedIdentifier(assignedIdentifier);
    }


    @Override
    public void remove(final Site site) {
        try {
            siteService.removeSite(site);
        } catch (Exception e) {
            String message = "Can not delete the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
            log.error(message, e);
        }
    }

    @Override
    public void store(final Site site) {
        try {

            Site existingSite = getRequestedObject();
            siteService.createOrMergeSites(existingSite, site);

        } catch (Exception e) {
            String message = "Can not update the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
            log.error(message, e);

        }

    }

    @Override
    public void verifyRemovable(final Site site) throws ResourceException {
        super.verifyRemovable(site);
        if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown Site Identifier " +UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
        }
        if (site.hasAssignments()) {
            String message = "Can not delete the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()) +
                    " because site has some assignments";
            log.error(message);

            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);

        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = super.represent(variant);
        if (getRequestedObject() != null ) {
           for (UserRole userRole : getLegacyCurrentUser().getUserRoles()) {
                if (userRole.getRole().equals(Role.SYSTEM_ADMINISTRATOR) || userRole.getRole().equals(Role.STUDY_ADMIN) || userRole.getRole().equals(Role.STUDY_COORDINATOR)) {
                    return representation;
                }
                for (Site site :  userRole.getSites()) {
                    if (getRequestedObject().equals(site)) {
                        return representation;
                    }
                }
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,"User is not allowed " + getLegacyCurrentUser().getDisplayName());
           }
       }
       throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"Unknown Site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }


}