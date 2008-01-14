package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlFactory;

/**
 * @author Rhett Sutphin
 */
public abstract class StudySiteCollectionResource<V> extends Resource {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private SiteDao siteDao;
    private StudyDao studyDao;
    protected StudyCalendarXmlFactory studyCalendarXmlFactory;

    private Study study;
    private Site site;
    private StudySite studySite;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        study = studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER.extractFrom(request));
        site = siteDao.getByName(SITE_NAME.extractFrom(request));
        if (study == null || site == null) {
            studySite = null;
        } else {
            studySite = study.getStudySite(site);
        }
        setAvailable(studySite != null);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override public boolean allowPost() { return true; }

    protected StudySite getStudySite() {
        return studySite;
    }

    private void verifyStudySiteExists() throws ResourceException {
        if (study == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "No study matching " +  STUDY_IDENTIFIER.extractFrom(getRequest()));
        } else if (site == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "No site matching " +  SITE_NAME.extractFrom(getRequest()));
        } else if (studySite == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                "Site " +  SITE_NAME.extractFrom(getRequest()) +
                " is not participating in " + STUDY_IDENTIFIER.extractFrom(getRequest())
            );
        }
    }

    protected abstract Representation createXmlRepresentation(StudySite studySite) throws ResourceException;

    public Representation represent(Variant variant) throws ResourceException {
        verifyStudySiteExists();

        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            return createXmlRepresentation(studySite);
        } else {
            return null;
        }
    }

    protected abstract String acceptValue(V value) throws ResourceException;

    public void acceptRepresentation(Representation entity) throws ResourceException {
        verifyStudySiteExists();

        if (entity.getMediaType().equals(MediaType.TEXT_XML)) {
            V value;
            try {
                value = (V) studyCalendarXmlFactory.readDocument(entity.getReader());
            } catch (IOException e) {
                log.warn("PUT failed with IOException");
                throw new ResourceException(e);
            }
            if (value == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse request");
            } else {
                String target = acceptValue(value);
                getResponse().redirectSeeOther(target);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type");
        }
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
    public void setStudyCalendarXmlFactory(StudyCalendarXmlFactory studyCalendarXmlFactory) {
        this.studyCalendarXmlFactory = studyCalendarXmlFactory;
    }
}
