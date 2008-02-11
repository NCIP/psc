package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
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
 * @author Rhett Sutphin
 */
public class StudySiteResource extends AbstractRemovableStorableDomainObjectResource<StudySite> {
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudyService studyService;

    private Study study;
    private Site site;


    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SITE_COORDINATOR);
        setAuthorizedFor(Method.PUT, Role.SITE_COORDINATOR);
    }

    protected StudySite loadRequestedObject(Request request) {
        study = studyDao.getByAssignedIdentifier(UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request));
        site = siteDao.getByAssignedIdentifier(UriTemplateParameters.SITE_NAME.extractFrom(request));
        if (study == null || site == null) {
            return null;
        } else {
            return study.getStudySite(site);
        }
    }

    protected void validateEntity(Representation entity) throws ResourceException {
        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No study matching " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()));
        } else if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No site named " + UriTemplateParameters.SITE_NAME.extractFrom(getRequest()));
        }
    }

    public void store(StudySite studySite) {
        if (getRequestedObject() == null) {
            StudySite newSS = new StudySite();
            newSS.setStudy(study);
            newSS.setSite(site);
            study.addStudySite(newSS);
            studyService.save(study);
        }
    }

    public void verifyRemovable(StudySite studySite) throws ResourceException {
        if (!studySite.getStudySubjectAssignments().isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "There are still subjects assigned to " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()) +
                            " at the site " + UriTemplateParameters.SITE_NAME.extractFrom(getRequest()));
        }
    }

    public void remove(StudySite studySite) {
        Study study = studySite.getStudy();
        study.getStudySites().remove(studySite);
        studyService.save(study);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

}
