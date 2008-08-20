package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.Request;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateHelper {
    public static final String CURRENT = "current";
    public static final String DEVELOPMENT = "development";

    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private AmendmentDao amendmentDao;

    protected Study getAmendedTemplate(Request request) throws NotFound {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        if (StringUtils.isBlank(studyIdentifier)) {
            throw new NotFound("No study specified");
        }

        Study study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            throw new NotFound("No study matching %s", studyIdentifier);
        }

        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);
        if (StringUtils.isBlank(amendmentIdentifier)) {
            throw new NotFound("No amendment specified");
        } else if (AmendedTemplateHelper.DEVELOPMENT.equals(amendmentIdentifier)) {
            return applyDevelopmentAmendment(study);
        } else {
            return applyReleasedAmendment(amendmentIdentifier, study);
        }
    }

    private Study applyDevelopmentAmendment(Study study) {
        if (study.getDevelopmentAmendment() == null) {
            throw new NotFound("Study template %s is not in development", study.getAssignedIdentifier());
        }
        return deltaService.revise(study, study.getDevelopmentAmendment());
    }

    private Study applyReleasedAmendment(String amendmentIdentifier, Study study) {
        Amendment amendment;
        if (AmendedTemplateHelper.CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
            if (amendment == null) {
                throw new NotFound(
                    "%s has never been released, so it has no current version.  Try 'development' instead of 'current'.",
                    study.getAssignedIdentifier());
            }
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier, study);
            if (amendment != null && !amendment.equals(study.getAmendment()) && !study.getAmendment().hasPreviousAmendment(amendment)) {
                throw new NotFound("The amendment %s is not part of %s",
                    amendmentIdentifier, study.getAssignedIdentifier());
            } else if (amendment == null) {
                throw new NotFound("No released amendment matching %s for %s",
                    amendmentIdentifier, study.getAssignedIdentifier());
            }
        }
        return amendmentService.getAmendedStudy(study, amendment);
    }

    ////// CONFIGURATION

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

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    //////

    public static class NotFound extends StudyCalendarUserException {
        public NotFound(String message, Object... messageParameters) {
            super(message, messageParameters);
        }
    }
}