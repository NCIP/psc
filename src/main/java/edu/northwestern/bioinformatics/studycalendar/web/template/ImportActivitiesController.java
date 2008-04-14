package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ImportActivitiesController extends PscSimpleFormController {
    private ImportActivitiesService importActivitiesService;
    private PeriodDao periodDao;

    public ImportActivitiesController() {
        setCommandClass(ImportActivitiesCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/importActivities");
        setSuccessView("redirectToStudyList");
        setCrumb(new Crumb());
    }

    protected ModelAndView onSubmit(Object o, BindException errors) throws Exception {
        ImportActivitiesCommand command = (ImportActivitiesCommand) o;
        command.apply();
        if (command.getReturnToPeriodId() == null) {
            Map<String, Object> model = errors.getModel();
            return new ModelAndView(getSuccessView(), model);
        } else {
            ModelMap model = new ModelMap("id", command.getReturnToPeriodId());
            return new ModelAndView("redirectToManagePeriod", model);
        }
    }


    protected ImportActivitiesCommand formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ImportActivitiesCommand command = new ImportActivitiesCommand();
        command.setImportActivitiesService(importActivitiesService);
        return command;
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String,Object>();

        Integer periodId = ((ImportActivitiesCommand) command).getReturnToPeriodId();
        if (periodId != null) {
            getControllerTools().addHierarchyToModel(periodDao.getById(periodId), refdata);
        }
        return refdata;
    }

    //// Field setters
    @Required
    public void setImportActivitiesService(ImportActivitiesService importActivitiesService) {
        this.importActivitiesService = importActivitiesService;
    }


    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            return "Import activities";

        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("returnToPeriodId", context.getPeriod().getId().toString());
        }
    }

    @Required
    public void setPeriodDao(final PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
