package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * TODO: this should be sensitive to the user's permissions, just like the html view.
 *
 * @author Rhett Sutphin
 */
public class StudiesResource extends AbstractCollectionResource<Study> {

    private StudyDao studyDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    public Collection<Study> getAllObjects() {

        return studyDao.getAll();
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


}
