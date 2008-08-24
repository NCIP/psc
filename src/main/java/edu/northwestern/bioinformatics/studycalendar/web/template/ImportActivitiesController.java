package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ImportActivitiesController extends PscSimpleFormController {
    private ImportActivitiesService importActivitiesService;
    private SourceDao sourceDao;
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;

    public ImportActivitiesController() {
        setCommandClass(ImportActivitiesCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/importActivities");
        setSuccessView("activity");
        setCrumb(new Crumb());
    }

    protected ModelAndView onSubmit(Object o, BindException errors) throws Exception {
        ImportActivitiesCommand command = (ImportActivitiesCommand) o;
        List<Source> sources = sourceDao.getAll();
        command.apply();
        Map<String, Object> model = errors.getModel();
        if (errors.hasErrors()) {
            return new ModelAndView(getSuccessView(), model);
        } else {
//            model = processRequest(model, sources);
            List<Source> sourcesAfterAdding = sourceDao.getAll();
            //default sourse to display if there is an error
            Source sourceToDisplay= sourcesAfterAdding.get(0);
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
                if (plannedActivityDao.getPlannedActivitiesForActivity(a.getId()).size()>0) {
                    enableDelete.put(a.getId(), false);
                } else {
                    enableDelete.put(a.getId(), true);
                }
            }
            model.put("activitiesPerSource", activities);
            model.put("enableDeletes", enableDelete);
            model.put("activityTypes", ActivityType.values());

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
}
