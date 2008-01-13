package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.restlet.data.Request;
import org.springframework.beans.factory.annotation.Required;

/**
 * Resource representing a study and its planned calendar, including all amendments.
 *
 * @author Rhett Sutphin
 */
public class TemplateResource extends AbstractStorableDomainObjectResource<Study> {
    private StudyDao studyDao;

    protected Study loadRequestedObject(Request request) {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        return studyDao.getStudyByAssignedIdentifier(studyIdent);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
