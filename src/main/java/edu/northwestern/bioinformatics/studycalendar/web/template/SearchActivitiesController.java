package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

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

    public SearchActivitiesController() {
        setCommandClass(SearchActivitiesCommand.class);
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
            getControllerTools().sendPostOnlyError(response);
            return null;
        }
    }

    private List<Activity> searchActivities(List<Activity>activities, String searchText) {
        if (searchText.equals(EMPTY)) return EMPTY_LIST;

        List<Activity> results = new ArrayList<Activity>();
        for (Activity activity : activities) {
            if (activity.getName().contains(searchText)
                    || activity.getCode().contains(searchText))
                results.add(activity);
        }
        return results;
    }

    private List<Activity> filterBySource(List<Activity> activities, Source source) {
        if (source == null) return activities;
        
        List<Activity> results = new ArrayList<Activity>();
        for (Activity activity : activities) {
            if (activity.getSource().equals(source)) results.add(activity);
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
}
