package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
// TODO: probably will want to make this use a collection XML serializer, etc.,
// in the future.  For now, just implementing form-encoded POST.
public class PlannedActivitiesResource extends AbstractDomainObjectResource<Period> {
    private AmendedTemplateHelper helper;
    private AmendmentService amendmentService;
    private ActivityDao activityDao;
    private PopulationDao populationDao;
    private TemplateService templateService;
    private StudyService studyService;
    private PeriodDao periodDao;

    @Override
    public void init(Context context, Request request, Response response) {
        helper.setRequest(request);

        super.init(context, request, response);
        setAuthorizedFor(Method.POST, Role.STUDY_COORDINATOR);
        setReadable(false); // pending
        getVariants().add(new Variant(MediaType.APPLICATION_WWW_FORM));
    }

    @Override
    protected Period loadRequestedObject(Request request) {
        try {
            return helper.drillDown(Period.class);
        } catch (AmendedTemplateHelper.NotFound notFound) {
            setClientErrorReason(notFound.getMessage());
            return null;
        }
    }

    @Override public boolean allowPost() { return true; }

    @Override
    @SuppressWarnings("unused")
    public void acceptRepresentation(Representation entity) throws ResourceException {
        if (!isAvailable()) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        }
        if (!helper.isDevelopmentRequest()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                "You can only add new planned activities to the development version of the template");
        }
        if (entity.getMediaType().includes(MediaType.APPLICATION_WWW_FORM)) {
            acceptForm(entity);
        }
    }

    private void acceptForm(Representation entity) throws ResourceException {
        PlannedActivityForm form = new PlannedActivityForm(entity, getStudy(), activityDao, populationDao);
        PlannedActivity newPlannedActivity = form.createDescribedPlannedActivity();

        Period realPeriod = periodDao.getByGridId(getRequestedObject());
        try {
            amendmentService.updateDevelopmentAmendment(realPeriod, Add.create(newPlannedActivity));
        } catch (StudyCalendarUserException e) {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }

        studyService.saveStudyFor(realPeriod);
        getResponse().setStatus(Status.SUCCESS_CREATED);
        getResponse().setLocationRef(
            getRequest().getResourceRef().addSegment(newPlannedActivity.getGridId()));
    }

    private Study getStudy() {
        return templateService.findStudy(getRequestedObject());
    }

    ////// CONFIGURATION

    @Required
    public void setAmendedTemplateHelper(AmendedTemplateHelper amendedTemplateHelper) {
        this.helper = amendedTemplateHelper;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
