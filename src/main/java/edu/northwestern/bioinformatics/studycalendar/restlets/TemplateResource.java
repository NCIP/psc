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

/**
 * Resource representing a study and its planned calendar, including all amendments.
 *
 * @author Rhett Sutphin
 */
public class TemplateResource extends Resource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String STUDY_TEMPLATE_PARAMETER = "study_identifier";

    private StudyDao studyDao;
    private StudyService studyService;

    private Study requestedStudy;
    private DaoFinder daoFinder;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setReadable(true);
        setModifiable(true);
        String studyIdent = Reference.decode((String) request.getAttributes().get(STUDY_TEMPLATE_PARAMETER));
        requestedStudy = studyDao.getStudyByAssignedIdentifier(studyIdent);
        if (requestedStudy == null) log.debug("Requested study {} not present", studyIdent);
        else log.debug("Requested study {} is {}", studyIdent, requestedStudy);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (requestedStudy == null) return null;

        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return new StringRepresentation(new StudyXMLWriter(daoFinder).createStudyXML(requestedStudy), MediaType.TEXT_XML);
        } else {
            return null;
        }
    }

    ////// CONFIGURATION

    public StudyDao getStudyDao() {
        return studyDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public StudyService getStudyService() {
        return studyService;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
