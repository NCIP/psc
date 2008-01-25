package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.restlet.data.Request;
import org.springframework.beans.factory.annotation.Required;

/**
 * Resource representing a single {@link Source}.
 *
 * @author Rhett Sutphin
 */
public class ActivitySourceResource extends AbstractStorableDomainObjectResource<Source> {
    private SourceDao sourceDao;

    @Override
    protected Source loadRequestedObject(Request request) {
        String sourceName = UriTemplateParameters.SOURCE_NAME.extractFrom(request);
        return sourceDao.getByName(sourceName);
    }

    ////// CONFIGURATION

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
}
