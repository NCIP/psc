package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;

/**
 * @author John Dzak
 */
public class AmendedTemplateResource extends AbstractDomainObjectResource<Study> {
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private AmendmentDao amendmentDao;
    protected static final String CURRENT = "current";

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = super.represent(variant);
        Date modifiedDate = getRequestedObject().getLastModifiedDate();
        representation.setModificationDate(modifiedDate);
        return representation;
    }

    @Override
    protected Study loadRequestedObject(Request request) {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);

        Study study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            log.debug("No study matching {}", studyIdentifier);
            return null;
        }

        Amendment amendment;
        if (CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier, study);
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

        return amendmentService.getAmendedStudy(study, amendment);
    }

    ////// Bean Setters
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
