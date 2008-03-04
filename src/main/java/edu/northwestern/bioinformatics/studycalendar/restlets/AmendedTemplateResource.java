package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.PlannedCalendarXmlSerializer;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class AmendedTemplateResource extends AbstractDomainObjectResource<PlannedCalendar> {
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private AmendmentDao amendmentDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        ((PlannedCalendarXmlSerializer) xmlSerializer).setSerializeEpoch(true);

        setAllAuthorizedFor(Method.GET);
    }

    protected PlannedCalendar loadRequestedObject(Request request) {
        String studyIdentifier =  UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);

        Study study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            throw new StudyCalendarValidationException("Study Not Found");
        }

        Amendment amendment = amendmentDao.getByNaturalKey(amendmentIdentifier);
        if (amendment == null) {
            throw new StudyCalendarValidationException("Amendment Not Found");
        }

        Study clone = study.transientClone();
        Study amended = amendmentService.getAmendedStudy(clone, amendment);

        return amended.getPlannedCalendar();
    }

    ////// Bean Setters
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
