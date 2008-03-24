package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Saurabh Agrawal
 */
public class AmendedResource extends AbstractDomainObjectResource<Amendment> {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    protected Amendment loadRequestedObject(Request request) {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);

        Study study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            log.debug("No study matching {}", studyIdentifier);
            return null;
        }

        Amendment amendment = null;
        if (AmendedTemplateResource.CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier);
            if (amendment != null && !amendment.equals(study.getAmendment()) && !study.getAmendment().hasPreviousAmendment(amendment)) {
                log.debug("Amendment {} doesn't apply to study {}",
                        amendmentIdentifier, study.getAssignedIdentifier());
                return null;
            }
        }
        if (amendment == null) {
            log.debug("No released amendment matching {}", amendmentIdentifier);
            return null;
        }

        return amendment;
    }

    ////// Bean Setters
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
