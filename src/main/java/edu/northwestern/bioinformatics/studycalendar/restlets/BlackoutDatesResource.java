package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Holiday;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.BlackoutDateXmlSerializer;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDatesResource extends AbstractCollectionResource<Holiday> {

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

    public Collection<Holiday> getAllObjects() {

        return site.getHolidaysAndWeekends();
    }

//    @Override
//    public void store(final Holiday site) {
//        try {
//
//            Holiday existingSite = getRequestedObject();
//            siteService.createOrMergeSites(existingSite, site);
//
//        } catch (Exception e) {
//            String message = "Can not update the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
//            log.error(message, e);
//
//        }
//
//    }

    public StudyCalendarXmlCollectionSerializer<Holiday> getXmlSerializer() {
        BlackoutDateXmlSerializer xmlSerializer = new BlackoutDateXmlSerializer(site);

        return xmlSerializer;
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }


}