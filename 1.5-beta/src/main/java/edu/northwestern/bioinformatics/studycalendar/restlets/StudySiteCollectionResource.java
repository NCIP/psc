package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Reference;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public abstract class StudySiteCollectionResource<V> extends Resource {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private SiteDao siteDao;
    private StudyDao studyDao;
    protected StudyCalendarXmlCollectionSerializer<V> xmlSerializer;

    private Study study;
    private Site site;
    private StudySite studySite;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        study = studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER.extractFrom(request));
        site = siteDao.getByName(SITE_NAME.extractFrom(request));
        if (study != null && site != null) {
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

    protected abstract Representation createXmlRepresentation(StudySite target) throws ResourceException;

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        verifyStudySiteExists();

        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            return createXmlRepresentation(studySite);
        } else {
            return null;
        }
    }

    protected abstract String acceptValue(V value) throws ResourceException;

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
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
                // TODO: the URL construction here seems bogus -- see if there's another way
                getResponse().redirectSeeOther(
                    new Reference(
                        new Reference(getRequest().getRootRef().toString() + '/'), target));
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
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<V> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
}
