package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.BlackoutDateXmlSerializer;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDatesResource extends AbstractStorableCollectionResource<BlackoutDate> {

    private SiteService siteService;

    private Site site;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.SITE_COORDINATOR);
        String assignedIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);
        site = siteService.getByAssignedIdentifier(assignedIdentifier);

    }

    public Collection<BlackoutDate> getAllObjects() {
        if (site != null) {
            return site.getBlackoutDates();
        }
        return null;
    }

    @Override
    public String store(final BlackoutDate blackoutDate) {
        try {

            site.addOrMergeExistingHoliday(blackoutDate);

//            for (BlackoutDate blackoutDate : holidays) {
//                site.addOrMergeExistingHoliday(blackoutDate);
//            }


            siteService.createOrUpdateSite(site);
            return String.format("sites/%s/blackout-dates/%s",
                    site.getAssignedIdentifier(), blackoutDate.getId());
        } catch (Exception e) {
            String message = "Can not POST the blackoutDate on the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
            log.error(message, e);

        }

        return null;
    }

    protected void validateEntity(final Representation entity) throws ResourceException {
        super.validateEntity(entity);
        if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No site with identifier " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
        }
    }

    public StudyCalendarXmlCollectionSerializer<BlackoutDate> getXmlSerializer() {
        BlackoutDateXmlSerializer xmlSerializer = new BlackoutDateXmlSerializer(site);

        return xmlSerializer;
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }


}