package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.ImportTemplateService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * Resource representing a study and its planned calendar, including all amendments.
 *
 * @author Rhett Sutphin
 */
public class TemplateResource extends AbstractDomainObjectResource<Study> {
    private StudyDao studyDao;

    private ImportTemplateService importTemplateService;

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = super.represent(variant);
        representation.setModificationDate(getRequestedObject().getLastModifiedDate());
        return representation;
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.STUDY_COORDINATOR);
    }

    @Override
    protected Study loadRequestedObject(Request request) {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        return studyDao.getByAssignedIdentifier(studyIdent);
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        Study study;
        // XXX: what is going on here? It looks like the entity is consumed twice.
        try {
            try {
                xmlSerializer.readDocument(entity.getStream());
            } catch (StudyCalendarValidationException e) {
                log.debug("PUT failed due to the element type is other than <study> or study doesn't have amendments");
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }

            study = importTemplateService.readAndSaveTemplate(getRequestedObject(), entity.getStream());
        } catch (IOException e) {                                   
            log.warn("PUT failed with IOException", e);
            throw new ResourceException(e);
        }
        getResponse().setEntity(createXmlRepresentation(study));
        if (getRequestedObject() == null) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setImportTemplateService(ImportTemplateService importTemplateService) {
        this.importTemplateService = importTemplateService;
    }
}
