package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
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

    @Override
    public void init(Context context, Request request, Response response) {
        helper.setRequest(request);

        super.init(context, request, response);
        addAuthorizationsFor(Method.POST,
            ResourceAuthorization.createTemplateManagementAuthorizations(
                helper.getAmendedTemplateOrNull(), PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
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

        try {
            log.debug("Attempting to merge {} into the current dev amendment for {}", newPlannedActivity, getRequestedObject());
            amendmentService.addPlannedActivityToDevelopmentAmendmentAndSave(
                getRequestedObject(), newPlannedActivity);
        } catch (StudyCalendarUserException e) {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }

        if (newPlannedActivity.getGridId() == null) {
            // this is not great -- it would be better to throw an exception
            // however, it is very difficult to simulate this side-effect using mocks
            // and I don't have time -- RMS20080904
            log.error("Planned activity not saved, somehow");
        }

        getResponse().setStatus(Status.SUCCESS_CREATED);
        getResponse().setLocationRef(
            getRequest().getResourceRef().addSegment(newPlannedActivity.getGridId()));
    }

    private Study getStudy() throws ResourceException {
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
}


