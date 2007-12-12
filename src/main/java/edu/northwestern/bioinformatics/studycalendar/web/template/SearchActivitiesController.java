package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import static java.util.Collections.EMPTY_LIST;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */
public class SearchActivitiesController extends PscAbstractCommandController<SearchActivitiesCommand> {
    private ActivityDao activityDao;
    private SourceDao sourceDao;

    public SearchActivitiesController() {
        setCommandClass(SearchActivitiesCommand.class);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "source", sourceDao);
        binder.registerCustomEditor(ActivityType.class, new ControlledVocabularyEditor(ActivityType.class));
    }

    protected ModelAndView handle(SearchActivitiesCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model =  new HashMap<String, Object>();

            String searchText = command.getSearchText() != null ? command.getSearchText() : EMPTY;
            ActivityType activityType = command.getActivityType();
            Source source = command.getSource();

            List<Activity> activities = activityDao.getAll();

            List<Activity> results = searchActivities(activities, searchText);

            results = filterBySource(results, source);
            results = filterByActivityType(results, activityType);

            model.put("activities", results);

            return new ModelAndView("template/ajax/activities", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }

    // TODO: remove null check for code if find out code is required (Reconsent doesn't have code)
    private List<Activity> searchActivities(List<Activity>activities, String searchText) {
        if (searchText.equals(EMPTY)) return EMPTY_LIST;

        String searchTextLower = searchText.toLowerCase();

        List<Activity> results = new ArrayList<Activity>();
        for (Activity activity : activities) {

            String activityName = activity.getName().toLowerCase();
            String activityCode = activity.getCode() != null ? activity.getCode().toLowerCase() : EMPTY;
            
            if (activityName.contains(searchTextLower)
                    || activityCode.contains(searchTextLower))
                results.add(activity);
        }
        return results;
    }

    // TODO: remove null check for source if find out code is required (Reconsent doesn't have source)
    private List<Activity> filterBySource(List<Activity> activities, Source source) {
        if (source == null) return activities;
        
        List<Activity> results = new ArrayList<Activity>();
        for (Activity activity : activities) {
            if (activity.getSource() != null && activity.getSource().equals(source)) results.add(activity);
        }
        return results;
    }

    private List<Activity> filterByActivityType(List<Activity> activities, ActivityType activityType) {
        if (activityType == null) return activities;

        List<Activity> results = new ArrayList<Activity>();
        for (Activity activity : activities) {
            if (activity.getType().equals(activityType)) results.add(activity);
        }
        return results;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
}
