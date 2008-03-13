package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class AmendedTemplateResource extends AbstractDomainObjectResource<Study> {
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private AmendmentDao amendmentDao;
    private final String CURRENT = "current";

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    protected Study loadRequestedObject(Request request) {
        String studyIdentifier =  UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);

        Study study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            throw new StudyCalendarValidationException("Study Not Found");
        }

        Amendment amendment;
        if (CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier);
            if (amendment == null) {
                throw new StudyCalendarValidationException("Amendment Not Found");
            }
        }

        Study clone = study.transientClone();

        return amendmentService.getAmendedStudy(clone, amendment);
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
