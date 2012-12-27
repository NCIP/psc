/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.BlackoutDateDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.restlet.data.Method;
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
    private ResourceException siteLoadException;

    @Override
    public void doInit() {
        super.doInit();
        // initialize site first for authorization
        try {
            site = loadRequestedSiteObject();
        } catch (ResourceException e) {
            siteLoadException = e;
        }
        addAuthorizationsFor(Method.GET, site,
                PERSON_AND_ORGANIZATION_INFORMATION_MANAGER,
                DATA_READER);
        addAuthorizationsFor(Method.POST, ResourceAuthorization.create(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER, site));
    }

    public Collection<BlackoutDate> getAllObjects() throws ResourceException {
        Site requestedSite = getRequestedSiteObject();
        return requestedSite.getBlackoutDates();
    }

    private Site loadRequestedSiteObject() throws ResourceException{
        String siteIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
        if (siteIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No site in request");
        }
        site = siteService.getByAssignedIdentifier(siteIdentifier);
        if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown site " + siteIdentifier );
        }
        return site;
    }

    private Site getRequestedSiteObject() throws ResourceException {
        if (siteLoadException != null) {
            throw siteLoadException;
        } else {
            return site;
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