package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.LabelDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.LabelService;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
// TODO: probably will want to make this use a collection XML serializer, etc.,
// in the future.  For now, just implementing form-encoded PUT & simple DELETE.
public class PlannedActivityResource extends AbstractDomainObjectResource<PlannedActivity> {
    private static final List<String> PLANNED_ACTIVITY_PROPERTIES
        = Arrays.asList("day", "activity", "population", "details", "condition");

    private AmendedTemplateHelper helper;
    private AmendmentService amendmentService;
    private ActivityDao activityDao;
    private PopulationDao populationDao;
    private PlannedActivityDao plannedActivityDao;
    private TemplateService templateService;
    private LabelService labelService;

    @Override
    public void init(Context context, Request request, Response response) {
        helper.setRequest(request);
        super.init(context, request, response);
        setAuthorizedFor(Method.PUT, Role.STUDY_COORDINATOR);
        setAuthorizedFor(Method.DELETE, Role.STUDY_COORDINATOR);
        setReadable(false); // pending
    }

    @Override public boolean allowPut() { return true; }

    @Override public boolean allowDelete() { return true; }

    @Override
    protected PlannedActivity loadRequestedObject(Request request) {
        try {
            return helper.drillDown(PlannedActivity.class);
        } catch (AmendedTemplateHelper.NotFound notFound) {
            setClientErrorReason(notFound.getMessage());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unused")
    public void storeRepresentation(Representation entity) throws ResourceException {
        if (!helper.isDevelopmentRequest()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                "You can only update planned activities in the development version of the template");
        }
        PlannedActivityForm form = new PlannedActivityForm(entity, helper.getRealStudy(), activityDao, populationDao, labelService);
        if (isAvailable()) {
            updatePlannedActivityFrom(form);
        } else {
            createNewPlannedActivityFrom(form);
        }
        String responseEntity;
        MediaType type = MediaType.APPLICATION_WWW_FORM;
        try {
            responseEntity = form.encode();
        } catch (IOException ie) {
            responseEntity = "Encoding data for return failed.  However, the request was successfully stored.";
            type = MediaType.TEXT_PLAIN;
        }
        getResponse().setEntity(responseEntity, type);
    }

    private void updatePlannedActivityFrom(PlannedActivityForm form) throws ResourceException {
        PlannedActivity fromForm = form.createDescribedPlannedActivity();
        BeanWrapper src = new BeanWrapperImpl(getRequestedObject());
        BeanWrapper dst = new BeanWrapperImpl(fromForm);
        List<PropertyChange> changes = new ArrayList<PropertyChange>(PLANNED_ACTIVITY_PROPERTIES.size());
        for (String property : PLANNED_ACTIVITY_PROPERTIES) {
            changes.add(PropertyChange.create(property,
                src.getPropertyValue(property), dst.getPropertyValue(property)));
        }

        List<PlannedActivityLabel> palabels = fromForm.getPlannedActivityLabels();
        for (PlannedActivityLabel palabel: palabels) {
            PlannedActivityLabel label = labelService.getOrCreatePlannedActivityLabel(palabel.getLabel(), getRequestedObject());
        }

        amendmentService.updateDevelopmentAmendmentAndSave(getRequestedObject(),
            changes.toArray(new Change[changes.size()]));
    }

    private void createNewPlannedActivityFrom(PlannedActivityForm form) throws ResourceException {
        // TODO: this logic is simplistic and will break when a planned activity is DELETEd and
        // then PUT again with the same ID in an amendment where the parent period already exists.
        String ident = UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.extractFrom(getRequest());
        if (plannedActivityDao.getByGridId(ident) != null) {
            setClientErrorReason(null);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                String.format("The planned activity %s exists, but is not part of the designated study", ident));
        }
        
        PlannedActivity pa = form.createDescribedPlannedActivity();
        amendmentService.updateDevelopmentAmendmentAndSave(helper.drillDown(Period.class),
            Add.create(pa));
    }

    @Override
    @SuppressWarnings("unused")
    public void removeRepresentations() throws ResourceException {
        if (!isAvailable()) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        }
        if (!helper.isDevelopmentRequest()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                "You can only delete planned activities from the development version of the template");
        }
        amendmentService.updateDevelopmentAmendmentAndSave(
            templateService.findParent(getRequestedObject()), Remove.create(getRequestedObject()));
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
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
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setLabelService(LabelService labelService) {
        this.labelService = labelService;
    }
}
