package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import org.restlet.Context;
import org.restlet.resource.*;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.ArrayList;
/**
 * @author Jalpa Patel
 */
public class ProvidedSitesResource extends Resource {
    private StudyCalendarXmlCollectionSerializer<Site> xmlSerializer;
    private SiteProvider siteProvider;
    private SiteDao siteDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    public List<Site> getAllObjects() {
        String q = QueryParameters.Q.extractFrom(getRequest());
        List<Site> availableSites = siteDao.searchSitesBySearchText(q);
        List<Site> providedSites = siteProvider.search(q);
        List<Site> duplicateSites = new ArrayList<Site>();
        for (Site providedSite : providedSites) {
            for (Site availableSite : availableSites ) {
               if( availableSite.getName().equals(providedSite.getName()) || availableSite.getAssignedIdentifier().equals(providedSite.getAssignedIdentifier())) {
                   duplicateSites.add(availableSite);
               }
            }
        }
        List<Site> all = new ArrayList<Site>(availableSites.size() + providedSites.size());
        all.addAll(availableSites);
        all.removeAll(duplicateSites);
        all.addAll(providedSites);
        return providedSites;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(getAllObjects());
        } else {
            return null;
        }
    }
    
    protected Representation createXmlRepresentation(List<Site> sites) {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(sites), MediaType.TEXT_XML);
    }
    public StudyCalendarXmlCollectionSerializer<Site> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Site> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
    
    @Required
    public void setSiteProvider(SiteProvider siteProvider) {
        this.siteProvider = siteProvider;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
