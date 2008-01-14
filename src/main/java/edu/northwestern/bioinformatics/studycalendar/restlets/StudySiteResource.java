package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class StudySiteResource extends AbstractDomainObjectResource<StudySite> {
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudyService studyService;

    private Study study;
    private Site site;

    protected StudySite loadRequestedObject(Request request) {
        study = studyDao.getByAssignedIdentifier(UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request));
        site = siteDao.getByName(UriTemplateParameters.SITE_NAME.extractFrom(request));
        if (study == null || site == null) {
            return null;
        } else {
            return study.getStudySite(site);
        }
    }

    @Override public boolean allowPut()    { return true;  }

    /**
     * PUT for this resource ignores the entity.
     */
    public void storeRepresentation(Representation entity) throws ResourceException {
        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "No study matching " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()));
        } else if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "No site named " + UriTemplateParameters.SITE_NAME.extractFrom(getRequest()));
        }

        StudySite studySite;
        if (getRequestedObject() == null) {
            StudySite newSS = new StudySite();
            newSS.setStudy(study);
            newSS.setSite(site);
            study.addStudySite(newSS);
            studyService.save(study);
            getResponse().setStatus(Status.SUCCESS_CREATED);
            studySite = newSS;
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
            studySite = getRequestedObject();
        }
        getResponse().setEntity(createXmlRepresentation(studySite));
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
