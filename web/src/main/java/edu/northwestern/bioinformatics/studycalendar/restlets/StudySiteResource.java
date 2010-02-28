package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class StudySiteResource extends AbstractRemovableStorableDomainObjectResource<StudySite> {
    private Study study;
    private Site site;
    private StudySiteDao studySiteDao;
    private StudySiteService studySiteService;


    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SITE_COORDINATOR);
        setAuthorizedFor(Method.PUT, Role.SITE_COORDINATOR);
        setAuthorizedFor(Method.DELETE, Role.SITE_COORDINATOR);
    }

    protected StudySite loadRequestedObject(Request request) {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String siteIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);

        StudySite studySite = studySiteService.getStudySite(studyIdentifier, siteIdentifier);

        if (studySite != null) {
            study = studySite.getStudy();
            site = studySite.getSite();
            if (study != null && site != null) {
                return studySite;
            }
        }

        return null;
    }

    protected void validateEntity(Representation entity) throws ResourceException {
        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No study matching " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()));
        } else if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No site named " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
        }
    }

    public void store(StudySite studySite) throws ResourceException {
        if (getRequestedObject() == null) {
            studySiteDao.save(studySite);
        }
    }

    public void verifyRemovable(StudySite studySite) throws ResourceException {
        if (!studySite.getStudySubjectAssignments().isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "There are still subjects assigned to " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()) +
                            " at the site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
        }
    }

    public void remove(StudySite studySite) {
        studySite.getStudy().getStudySites().remove(studySite);
        studySite.getSite().getStudySites().remove(studySite);
        studySiteDao.delete(studySite);
    }

    ////// CONFIGURATION

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }
}
