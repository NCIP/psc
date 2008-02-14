package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResource extends AbstractCollectionResource<Source> {

    private SourceDao sourceDao;

    private StudyCalendarXmlCollectionSerializer xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    public Collection<Source> getAllObjects() {
        return sourceDao.getAll();
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public StudyCalendarXmlCollectionSerializer<Source> getXmlSerializer() {
        return xmlSerializer;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Source> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
}
