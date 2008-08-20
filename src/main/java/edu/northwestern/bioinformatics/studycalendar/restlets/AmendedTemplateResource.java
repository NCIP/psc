package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import org.apache.commons.lang.StringUtils;
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
 * @author Rhett Sutphin
 */
public class AmendedTemplateResource extends AbstractDomainObjectResource<Study> {
    public static final String CURRENT = "current";
    public static final String DEVELOPMENT = "development";

    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private AmendmentDao amendmentDao;

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
        if (StringUtils.isBlank(studyIdentifier)) {
            setClientErrorReason("No study specified");
            return null;
        }

        Study study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            setClientErrorReason("No study matching %s", studyIdentifier);
            return null;
        }

        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);
        if (StringUtils.isBlank(amendmentIdentifier)) {
            setClientErrorReason("No amendment specified");
            return null;
        } else if (DEVELOPMENT.equals(amendmentIdentifier)) {
            return applyDevelopmentAmendment(study);
        } else {
            return applyReleasedAmendment(amendmentIdentifier, study);
        }
    }

    private Study applyDevelopmentAmendment(Study study) {
        if (study.getDevelopmentAmendment() == null) {
            setClientErrorReason("Study %s is not in development", study.getAssignedIdentifier());
            return null;
        }
        return deltaService.revise(study, study.getDevelopmentAmendment());
    }

    private Study applyReleasedAmendment(String amendmentIdentifier, Study study) {
        Amendment amendment;
        if (CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
            if (amendment == null) {
                setClientErrorReason(
                    "%s has never been released, so it has no current version.  Try 'development' instead of 'current'.",
                    study.getAssignedIdentifier());
            }
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier, study);
            if (amendment != null && !amendment.equals(study.getAmendment()) && !study.getAmendment().hasPreviousAmendment(amendment)) {
                setClientErrorReason("The amendment %s is not part of %s",
                    amendmentIdentifier, study.getAssignedIdentifier());
                amendment = null;
            } else if (amendment == null) {
                setClientErrorReason("No released amendment matching %s for %s",
                    amendmentIdentifier, study.getAssignedIdentifier());
            }
        }
        return amendment == null ? null : amendmentService.getAmendedStudy(study, amendment);
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

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
