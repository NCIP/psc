package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.BlackoutDateDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDatesResource extends AbstractStorableCollectionResource<BlackoutDate> {

    private SiteService siteService;
    private StudyCalendarXmlCollectionSerializer<BlackoutDate> xmlSerializer;
    private Site site;
    private BlackoutDateDao blackoutDateDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        addAuthorizationsFor(Method.GET, site,
                PERSON_AND_ORGANIZATION_INFORMATION_MANAGER,
                DATA_READER);
        addAuthorizationsFor(Method.POST, ResourceAuthorization.create(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER, site));

    }

    public Collection<BlackoutDate> getAllObjects() throws ResourceException {
        String siteIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
        if (siteIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No site in request");
        }
        site = siteService.getByAssignedIdentifier(siteIdentifier);
        if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown site " + siteIdentifier );
        } else {
            return site.getBlackoutDates();
        }
    }

    @Override
    public String store(BlackoutDate blackoutDate) throws ResourceException {
        try {
            siteService.resolveSiteForBlackoutDate(blackoutDate);
            blackoutDateDao.save(blackoutDate);
            return String.format("sites/%s/blackout-dates/%s",
                    blackoutDate.getSite().getAssignedIdentifier(), blackoutDate.getGridId());
        } catch (StudyCalendarValidationException scve) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, scve.getMessage());
        } catch (Exception e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
        }
    }

    public StudyCalendarXmlCollectionSerializer<BlackoutDate> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<BlackoutDate> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setBlackoutDateDao(BlackoutDateDao blackoutDateDao) {
        this.blackoutDateDao = blackoutDateDao;
    }
}