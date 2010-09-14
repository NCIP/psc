package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
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
        = Arrays.asList("day", "activity", "population", "details", "condition", "weight");

    private AmendedTemplateHelper helper;
    private AmendmentService amendmentService;
    private ActivityDao activityDao;
    private PopulationDao populationDao;
    private PlannedActivityDao plannedActivityDao;
    private TemplateService templateService;

    @Override
    public void init(Context context, Request request, Response response) {
        helper.setRequest(request);
        super.init(context, request, response);
        getVariants().clear();
        getVariants().add(new Variant(MediaType.APPLICATION_WWW_FORM));
        addAuthorizationsFor(Method.GET,
            ResourceAuthorization.createTemplateManagementAuthorizations(
                helper.getAmendedTemplateOrNull()));
        addAuthorizationsFor(Method.PUT,
            ResourceAuthorization.createTemplateManagementAuthorizations(
                helper.getAmendedTemplateOrNull(), PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
        addAuthorizationsFor(Method.DELETE,
            ResourceAuthorization.createTemplateManagementAuthorizations(
                helper.getAmendedTemplateOrNull(), PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
        setReadable(true);
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
        PlannedActivityForm form ;
        if (isAvailable()) {
            form = new PlannedActivityForm(entity, getStudy(), activityDao, populationDao);
            updatePlannedActivityFrom(form);
        } else {
            form = new PlannedActivityForm(entity,helper.getRealStudy(), activityDao, populationDao);
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


    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().isCompatible(MediaType.APPLICATION_WWW_FORM)) {
            String ident = UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.extractFrom(getRequest());
            PlannedActivity pa = plannedActivityDao.getByGridId(ident);
            PlannedActivityForm form = new PlannedActivityForm(pa, getStudy(), activityDao, populationDao);
            Representation rep = form.getWebRepresentation();
            return rep;
        }
        return null;
    }

    private void updatePlannedActivityFrom(PlannedActivityForm form) throws ResourceException {
        PlannedActivity fromForm = form.createDescribedPlannedActivity();
        BeanWrapper src = new BeanWrapperImpl(getRequestedObject());
        BeanWrapper dst = new BeanWrapperImpl(fromForm);
        List<Change> changes = new ArrayList<Change>(PLANNED_ACTIVITY_PROPERTIES.size());
        for (String property : PLANNED_ACTIVITY_PROPERTIES) {
            changes.add(PropertyChange.create(property,
                src.getPropertyValue(property), dst.getPropertyValue(property)));
        }

        // Look for labels that are in the current persistent version but not in the PUT form
        for (PlannedActivityLabel current : getRequestedObject().getPlannedActivityLabels()) {
            boolean found = false;
            for (PlannedActivityLabel candidate : fromForm.getPlannedActivityLabels()) {
                found = equivLabel(current, candidate);
                if (found) break;
            }
            if (!found) changes.add(Remove.create(current));
        }

        // Look for labels that are in the PUT form but not in the current persistent version
        for (PlannedActivityLabel candidate : fromForm.getPlannedActivityLabels()) {
            boolean found = false;
            for (PlannedActivityLabel current : getRequestedObject().getPlannedActivityLabels()) {
                found = equivLabel(current, candidate);
                if (found) break;
            }
            if (!found) {
                // Detach since the parent isn't the actual PA.  Let the delta system handle it.
                candidate.setPlannedActivity(null);
                changes.add(Add.create(candidate));
            }
        }

        amendmentService.updateDevelopmentAmendmentAndSave(getRequestedObject(),
            changes.toArray(new Change[changes.size()]));
    }

    private boolean equivLabel(PlannedActivityLabel a, PlannedActivityLabel b) {
        return ComparisonTools.nullSafeEquals(a.getRepetitionNumber(), b.getRepetitionNumber())
            && ComparisonTools.nullSafeEquals(a.getLabel(), b.getLabel());
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
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
