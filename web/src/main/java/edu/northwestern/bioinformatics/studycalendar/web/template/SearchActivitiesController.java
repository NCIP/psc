/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static org.apache.commons.lang.StringUtils.EMPTY;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author John Dzak
 */
public class SearchActivitiesController extends PscAbstractCommandController<SearchActivitiesCommand> implements PscAuthorizedHandler {
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;

    public SearchActivitiesController() {
        setCommandClass(SearchActivitiesCommand.class);
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "source", sourceDao);
        getControllerTools().registerDomainObjectEditor(binder, "activityType", activityTypeDao);
    }

    protected ModelAndView handle(SearchActivitiesCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model =  new HashMap<String, Object>();

            String searchText = command.getSearchText() != null ? command.getSearchText() : EMPTY;
            ActivityType activityType = command.getActivityType();
            Source source = command.getSource();

            List<Activity> results = activityDao.getActivitiesBySearchText(searchText);

            results = filterBySource(results, source);
            results = filterByActivityType(results, activityType);

            model.put("activities", results);
            return new ModelAndView("template/ajax/activities", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
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

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}