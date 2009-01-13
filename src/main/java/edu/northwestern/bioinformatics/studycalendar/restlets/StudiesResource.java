package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySnapshotXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import org.restlet.Context;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Reference;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.io.IOException;

/**
 * TODO: this should be sensitive to the user's permissions, just like the html view.
 *
 * @author Rhett Sutphin
 */
public class StudiesResource extends AbstractCollectionResource<Study> {

    private StudyDao studyDao;
    private StudyService studyService;

    private StudyCalendarXmlCollectionSerializer<Study> xmlSerializer;
    private StudySnapshotXmlSerializer studySnapshotXmlSerializer;
    private AuthorizationService authorizationService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.STUDY_COORDINATOR, Role.SUBJECT_COORDINATOR, Role.STUDY_ADMIN, Role.SITE_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.STUDY_COORDINATOR);
    }

    @Override public boolean allowPost() { return true; }

    @Override
    public Collection<Study> getAllObjects() {
        return authorizationService.filterStudiesForVisibility(studyDao.getAll(), getCurrentUser());
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        if (MediaType.TEXT_XML.includes(entity.getMediaType())) {
            Study read;
            try {
                read = studySnapshotXmlSerializer.readDocument(entity.getStream());
                if (studyDao.getByAssignedIdentifier(read.getAssignedIdentifier()) != null) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "There is already a study with the identifier " + read.getAssignedIdentifier());
                }
                studyService.createInDesignStudyFromExamplePlanTree(read);
            } catch (IOException e) {
                log.warn("POST failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarUserException scue) {
                log.debug("POST failed due to validation problem", scue);
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, scue.getMessage(), scue);
            }
            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(String.format(
                "studies/%s/template", Reference.encode(read.getAssignedIdentifier())));
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<Study> getXmlSerializer() {
        return xmlSerializer;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Study> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setStudySnapshotXmlSerializer(StudySnapshotXmlSerializer studySnapshotXmlSerializer) {
        this.studySnapshotXmlSerializer = studySnapshotXmlSerializer;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
}
