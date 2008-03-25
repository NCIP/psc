package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Saurabh Agrawal
 */
public class AmendedResource extends AbstractRemovableStorableDomainObjectResource<Amendment> {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private Study study;
    private StudyService studyService;

    private AmendmentService amendmentService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    protected Amendment loadRequestedObject(Request request) {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);

        study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            log.debug("No study matching {}", studyIdentifier);
            return null;
        }

        Amendment amendment;
        if (AmendedTemplateResource.CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier);
            if (amendment != null && !amendment.equals(study.getAmendment()) && !study.getDevelopmentAmendment().hasPreviousAmendment(amendment)
                    && !amendment.equals(study.getDevelopmentAmendment())) {
                log.debug("Amendment {} doesn't apply to study {}",
                        amendmentIdentifier, study.getAssignedIdentifier());
                return null;
            }
        }
        if (amendment == null) {
            log.debug("No released  or development amendment matching {}", amendmentIdentifier);
            return null;
        }

        return amendment;
    }

    @Override
    public void remove(final Amendment amendment) {
        log.debug("Deleting amendment {} for study {}",
                amendment.getNaturalKey(), study.getAssignedIdentifier());
        amendmentService.deleteDevelopmentAmendmentOnly(study);

    }

    @Override
    public void verifyRemovable(final Amendment amendment) throws ResourceException {
        super.verifyRemovable(amendment);
        if (amendment == null) {
            String message = "Amendment is null";
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);


        } else if (amendment.equals(study.getAmendment())) {
            String message = String.format("Amendment {} can be deleted if and only if it isn't released for the study {}",
                    amendment.getNaturalKey(), study.getAssignedIdentifier());
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);


        }
        //   You dont need this logic because amendment could be either development amendment or released amendment.
        // If its not a development amendment and not a released amendment, loadRequestObject will return null
//        else if (!amendment.equals(study.getDevelopmentAmendment())) {
//            String message = String.format("Amendment {} can not be deleted because it is not a development amendment of the study {}",
//                    amendment.getNaturalKey(), study.getAssignedIdentifier());
//            log.error(message);
//            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
//                    message);
//
//
//       }

    }

    @Override
    protected void validateEntity(final Representation entity) throws ResourceException {
        super.validateEntity(entity);
    }


    @Override
    public void store(final Amendment amendment) {

        if (study.getDevelopmentAmendment() != null) {
            //first delete the existing development amendment
            amendmentService.deleteDevelopmentAmendmentOnly(study);

        }
        study.setDevelopmentAmendment(amendment);

        studyService.save(study);

    }


    ////// Bean Setters
    @Required
    public void setStudyService(final StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(final AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

}
