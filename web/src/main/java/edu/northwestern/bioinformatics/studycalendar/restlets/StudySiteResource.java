/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class StudySiteResource extends AbstractRemovableStorableDomainObjectResource<StudySite> {
    private StudyDao studyDao;
    private SiteDao siteDao;

    private Study study;
    private Site site;
    private StudySiteDao studySiteDao;
    private StudySiteService studySiteService;

    @Override
    public void doInit() {
        super.doInit();

        StudySite ss = getRequestedObjectDuringInit();
        Site site = null;
        if (ss != null && ss.getSite() != null) {
            site = ss.getSite();
        }

        Study study = null;
        if (ss != null && ss.getStudy() != null) {
            study = ss.getStudy();
        }

        addAuthorizationsFor(Method.GET, site, study,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_QA_MANAGER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER);
        addAuthorizationsFor(Method.PUT, site, study, STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
        addAuthorizationsFor(Method.DELETE, site, study, STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
    }

    @Override
    protected StudySite loadRequestedObject(Request request) throws ResourceException {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String siteIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);

        // Refresh provided StudySites before Study and Site are loaded in-case eager
        // loading is enabled for the two.
        StudySite studySite = studySiteService.getStudySite(studyIdentifier, siteIdentifier);

        study = studyDao.getByAssignedIdentifier(studyIdentifier);
        site = siteDao.getByAssignedIdentifier(siteIdentifier);

        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "No study matching " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()));
        } else if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "No site matching " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
        } else {
            return studySite;
        }
    }

    @Override
    public StudySite store(StudySite fromEntity) throws ResourceException {
        if (getRequestedObject() == null) {
            StudySite fromUri = new StudySite(study, site);
            checkUriPutEntityMismatch(
                fromUri.getSite().getAssignedIdentifier(),
                fromEntity.getSite().getAssignedIdentifier(),
                "sites"
            );
            checkUriPutEntityMismatch(
                fromUri.getStudy().getAssignedIdentifier(),
                fromEntity.getStudy().getAssignedIdentifier(),
                "studies"
            );
            StudySite resolvedStudySite = studySiteService.resolveStudySite(fromUri);
            studySiteDao.save(resolvedStudySite);
            return resolvedStudySite;
        } else {
            return fromEntity;
        }
    }

    private void checkUriPutEntityMismatch(String identifierFromUri, String identifierFromEntity, String kind) {
        if (identifierFromEntity != null && !identifierFromEntity.equals(identifierFromUri)) {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                "Entity- and URI-designated " + kind + " do not match. Either make them match or omit the one in the entity.");
        }
    }

    @Override
    public void verifyRemovable(StudySite studySite) throws ResourceException {
        if (!studySite.getStudySubjectAssignments().isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "There are still subjects assigned to " + UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest()) +
                            " at the site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
        }
    }

    @Override
    public void remove(StudySite studySite) {
        studySite.getStudy().getStudySites().remove(studySite);
        studySite.getSite().getStudySites().remove(studySite);
        studySiteDao.delete(studySite);
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
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }
}
