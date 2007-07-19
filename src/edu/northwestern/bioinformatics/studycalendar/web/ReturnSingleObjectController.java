package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

import java.util.Map;
import java.util.Collections;

/**
 * This controller loads a single domain object and passes it to the configured view.
 * The controller expects to receive the ID of the object in a request parameter
 * named as configured with {@link #setParameterName}.  The loaded object is passed to
 * the view under the same name.
 *
 * @author Rhett Sutphin
 */
// If this actually gets reused, it'll have to be subclassed for different access permissions
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReturnSingleObjectController<T extends DomainObject> implements Controller {
    private StudyCalendarDao<T> dao;
    private String parameterName;
    private String viewName;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int armId = ServletRequestUtils.getRequiredIntParameter(request, getParameterName());
        Map<String, Object> model
            = Collections.singletonMap(getParameterName(), wrapObject(dao.getById(armId)));

        return new ModelAndView(getViewName(), model);
    }

    private String getParameterName() {
        return parameterName;
    }

    private String getViewName() {
        return viewName;
    }

    ////// TEMPLATE METHODS

    protected Object wrapObject(T t) {
        return t;
    }

    ////// CONFIGURATION

    @Required
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Required
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Required
    public void setDao(StudyCalendarDao<T> dao) {
        this.dao = dao;
    }
}
