package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

/**
 * Resource representing a single {@link Source}.
 *
 * @author Rhett Sutphin
 */
public class ActivitySourceResource extends AbstractStorableDomainObjectResource<Source> {
    private SourceDao sourceDao;
    private static PlannedActivityDao plannedActivityDao;
    private SourceService sourceService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.STUDY_COORDINATOR);
    }

    @Override
    protected Source loadRequestedObject(Request request) {
        String sourceName = UriTemplateParameters.ACTIVITY_SOURCE_NAME.extractFrom(request);
        return sourceDao.getByName(sourceName);
    }

    @Override
    public void store(Source source) {

        if (getRequestedObject() == null) {
            sourceDao.save(source);
        } else {
            Source existingSource = getRequestedObject();
            sourceService.updateSource(source,existingSource);


        }
    }


    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }


    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
    }
}