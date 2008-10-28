package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ImportActivitiesController extends PscSimpleFormController {
    private ImportActivitiesService importActivitiesService;
    private SourceDao sourceDao;
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private ActivityTypeDao activityTypeDao;

    public ImportActivitiesController() {
        setCommandClass(ImportActivitiesCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/importActivities");
        setSuccessView("activity");
        setCrumb(new Crumb());
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ImportActivitiesCommand command = (ImportActivitiesCommand) oCommand;
        List<Source> sources = sourceDao.getAll();
        try {
            command.apply();
        } catch (Exception e) {
            errors.reject("error.problem.reading.csv.file", new String[]{e.getMessage()}, e.getMessage());

        }
        Map<String, Object> model = errors.getModel();
        if (errors.hasErrors()) {
            return showForm(request, response, errors);
        } else {
//            model = processRequest(model, sources);
            List<Source> sourcesAfterAdding = sourceDao.getAll();
            //default sourse to display if there is an error
            Source sourceToDisplay = sourcesAfterAdding.get(0);
            for (Source source : sourcesAfterAdding) {
                if (!sources.contains(source)) {
                    sourceToDisplay = source;
                    break;
                }
            }
            model.put("source", sourceToDisplay);
            model.put("sourceId", sourceToDisplay.getId());
            model.put("sources", sourceDao.getAll());

            List<Activity> activities = activityDao.getBySourceId(sourceToDisplay.getId());

            Map<Integer, Boolean> enableDelete = new HashMap<Integer, Boolean>();
            for (Activity a : activities) {
                if (plannedActivityDao.getPlannedActivitiesForActivity(a.getId()).size() > 0) {
                    enableDelete.put(a.getId(), false);
                } else {
                    enableDelete.put(a.getId(), true);
                }
            }
            model.put("activitiesPerSource", activities);
            model.put("enableDeletes", enableDelete);
            model.put("activityTypes", activityTypeDao.getAll());

            model.put("displayCreateNewActivity", Boolean.TRUE);
            return new ModelAndView(getSuccessView(), model);
        }
    }


//    private Map<String, Object> processRequest( Map<String, Object> model, List<Source> sources) throws Exception{
//
//
//        return model;
//    }

    protected ImportActivitiesCommand formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ImportActivitiesCommand command = new ImportActivitiesCommand();
        command.setImportActivitiesService(importActivitiesService);
        return command;
    }

    //// CONFIGURATION
    @Required
    public void setImportActivitiesService(ImportActivitiesService importActivitiesService) {
        this.importActivitiesService = importActivitiesService;
    }


    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            return "Import activities";

        }
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
