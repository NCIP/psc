package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Collections;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class EditPeriodController extends AbstractPeriodController<EditPeriodCommand> implements PscAuthorizedHandler {
    private PeriodDao periodDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;

    public EditPeriodController() {
        super(EditPeriodCommand.class);
        setFormView("editPeriod");
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "period");
        Period period = periodDao.getById(periodId);
        if (!isFormSubmission(request)) {
            period = deltaService.revise(period);
        }
        return new EditPeriodCommand(period, amendmentService, getTemplateService());
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        // prevent spring from trying to bind "period" itself (manually bound in #formBackingObject)
        binder.setAllowedFields(new String[] { "period.*" });
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        Map<String, Object> refdata = super.referenceData(request, oCommand, errors);
        // include period in refdata for breadcrumbs
        EditPeriodCommand command = ((EditPeriodCommand) oCommand);
        getControllerTools().addHierarchyToModel(command.getPeriod(), refdata);
        refdata.put("amendment", getTemplateService().findStudy(command.getPeriod()).getDevelopmentAmendment());
        refdata.put("verb", "edit");
        return refdata;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            return new StringBuilder("Edit ").append(context.getPeriod().getDisplayName())
                .toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
           return Collections.singletonMap("period", context.getPeriod().getId().toString());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

}
