package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateResource extends AbstractRemovableStorableDomainObjectResource<BlackoutDate> {

    private SiteService siteService;

    private Site site;

    @Override
    public boolean allowPut() {
        return false;
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.SITE_COORDINATOR);

    }


    @Override
    protected BlackoutDate loadRequestedObject(Request request) {
        String blackoutDateIdentifier = UriTemplateParameters.BLACKOUT_DATE_IDENTIFIER.extractFrom(request);
        String assignedIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);
        site = siteService.getByAssignedIdentifier(assignedIdentifier);

        if (site == null || blackoutDateIdentifier == null) {
            return null;
            //     throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Site dost not exists for given identifier:" + assignedIdentifier);
        }
        List<BlackoutDate> holidaysAndWeekends = site.getBlackoutDates();

        for (BlackoutDate blackoutDate : holidaysAndWeekends) {
            if (blackoutDateIdentifier.trim().equals(blackoutDate.getId().intValue() + "")) {
                return blackoutDate;
            }
        }
        return null;
    }

    @Override
    public void remove(final BlackoutDate blackoutDate) {
        try {
            site.removeHoliday(blackoutDate);
            siteService.createOrUpdateSite(site);
        } catch (Exception e) {
            String message = "Can not delete the holiday" + UriTemplateParameters.BLACKOUT_DATE_IDENTIFIER.extractFrom(getRequest()) +
                    " on the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest())
                    + "because exception message :" + e.getMessage() + " and exception :" + e.getClass();
            log.error(message, e);
        }
    }

    @Override
    public void store(final BlackoutDate blackoutDate) {
        // throw new Exception("Store not implemented");

    }

    @Override
    public void verifyRemovable(final BlackoutDate blackoutDate) throws ResourceException {
        super.verifyRemovable(blackoutDate);
        boolean holidayExists = false;

        holidayExists = site.checkIfHolidayExists(blackoutDate);
        if (!holidayExists) {
            String message = "Can not delete the blackoutDate" + UriTemplateParameters.BLACKOUT_DATE_IDENTIFIER.extractFrom(getRequest()) +
                    " on the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()) + " because blackoutDate does not exists on this site.";
            log.error(message);

            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);

        }
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }


}