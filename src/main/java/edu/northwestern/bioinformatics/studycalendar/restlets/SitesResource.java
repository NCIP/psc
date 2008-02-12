package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class SitesResource extends AbstractCollectionResource<Site> {
    private SiteDao siteDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    public Collection<Site> getAllObjects() {
        return siteDao.getAll();
    }


    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }


}
