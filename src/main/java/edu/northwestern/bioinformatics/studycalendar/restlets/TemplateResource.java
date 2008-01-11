package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;

import java.io.IOException;

/**
 * Resource representing a study and its planned calendar, including all amendments.
 *
 * @author Rhett Sutphin
 */
public class TemplateResource extends Resource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String STUDY_TEMPLATE_PARAMETER = "study_identifier";

    private StudyDao studyDao;
    private StudyXMLReader studyXMLReader;
    private StudyXMLWriter studyXMLWriter;

    private Study requestedStudy;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        getVariants().add(new Variant(MediaType.TEXT_XML));

        String studyIdent = Reference.decode((String) request.getAttributes().get(UriTemplateParameters.STUDY_IDENTIFIER.attributeName()));
        requestedStudy = studyDao.getStudyByAssignedIdentifier(studyIdent);
        if (requestedStudy == null) {
            log.debug("Requested study {} not present", studyIdent);
            setAvailable(false);
        } else {
            log.debug("Requested study {} is {}", studyIdent, requestedStudy);
        }
    }

    @Override public boolean allowPut()    { return true; }
    @Override public boolean allowPost()   { return false; }
    @Override public boolean allowDelete() { return false; }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (requestedStudy == null) return null;

        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(requestedStudy);
        } else {
            return null;
        }
    }

    private StringRepresentation createXmlRepresentation(Study study) {
        return new StringRepresentation(studyXMLWriter.createStudyXML(study), MediaType.TEXT_XML);
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {
            Study read;
            try {
                read = studyXMLReader.read(entity.getStream());
            } catch (IOException e) {
                log.debug("PUT failed with IOException", e);
                throw new ResourceException(e);
            }
            getResponse().setEntity(createXmlRepresentation(read));
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudyXMLReader(StudyXMLReader studyXMLReader) {
        this.studyXMLReader = studyXMLReader;
    }

    @Required
    public void setStudyXMLWriter(StudyXMLWriter studyXMLWriter) {
        this.studyXMLWriter = studyXMLWriter;
    }
}
