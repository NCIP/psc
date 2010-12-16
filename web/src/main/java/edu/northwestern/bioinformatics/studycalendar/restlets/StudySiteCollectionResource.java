package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;

/**
 * @author Rhett Sutphin
 */
public abstract class StudySiteCollectionResource<V> extends AbstractPscResource {
    private SiteDao siteDao;
    private StudyDao studyDao;
    protected StudyCalendarXmlCollectionSerializer<V> xmlSerializer;

    private Study study;
    private Site site;
    private StudySite studySite;

    @Override
    public void doInit() {
        super.doInit();

        study = studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER.extractFrom(getRequest()));
        log.debug("Resolved study from {} as {}", STUDY_IDENTIFIER.extractFrom(getRequest()), study);
        site = siteDao.getByAssignedIdentifier(SITE_IDENTIFIER.extractFrom(getRequest()));
        log.debug("Resolved site from {} as {}", SITE_IDENTIFIER.extractFrom(getRequest()), site);

        if (study != null && site != null) {
            studySite = study.getStudySite(site);
        }
        setExisting(studySite != null);
        log.debug("Site {} participating in study", isExisting() ? "is" : "is not");

        getVariants().add(new Variant(MediaType.TEXT_XML));
        getAllowedMethods().add(Method.GET);
        getAllowedMethods().add(Method.POST);
    }

    protected StudySite getStudySite() {
        return studySite;
    }

    protected Study getStudy() {
        return study;
    }

    protected Site getSite() {
        return site;
    }

    private void verifyStudySiteExists() throws ResourceException {
        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No study matching " + STUDY_IDENTIFIER.extractFrom(getRequest()));
        } else if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "No site matching " + SITE_IDENTIFIER.extractFrom(getRequest()));
        } else if (studySite == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "Site " + SITE_IDENTIFIER.extractFrom(getRequest()) +
                            " is not participating in " + STUDY_IDENTIFIER.extractFrom(getRequest())
            );
        }
    }

    protected abstract Representation createXmlRepresentation(StudySite target) throws ResourceException;

    @Override
    public Representation get(Variant variant) throws ResourceException {
        verifyStudySiteExists();

        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            return createXmlRepresentation(studySite);
        } else {
            return null;
        }
    }

    protected abstract String acceptValue(V value) throws ResourceException;

    @Override
    public Representation post(Representation entity, Variant variant) throws ResourceException {
        verifyStudySiteExists();

        if (entity.getMediaType().equals(MediaType.TEXT_XML)) {
            V value;
            try {
                value = xmlSerializer.readDocument(entity.getStream());
            } catch (IOException e) {
                log.warn("PUT failed with IOException");
                throw new ResourceException(e);
            }
            if (value == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse request");
            } else {
                String target = acceptValue(value);
                getResponse().setStatus(Status.SUCCESS_CREATED);
                getResponse().setLocationRef(new Reference(getRequest().getRootRef() + "/" + target));
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type");
        }

        return null;
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<V> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
}
