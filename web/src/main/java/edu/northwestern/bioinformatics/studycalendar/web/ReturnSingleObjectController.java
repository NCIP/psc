package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * This controller loads a single domain object and passes it to the configured view.
 * The controller expects to receive the ID of the object in a request parameter
 * named as configured with {@link #setParameterName}.  The loaded object is passed to
 * the view under the same name.
 *
 * @author Rhett Sutphin
 */
// If this actually gets reused, it'll have to be subclassed for different access permissions
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ReturnSingleObjectController<T extends DomainObject> implements Controller, PscAuthorizedHandler {
    private StudyCalendarDao<T> dao;
    private String parameterName;
    private String viewName;

    //todo -- verify
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    @SuppressWarnings({ "unchecked" })
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, getParameterName());
        T obj = dao.getById(id);
        Map<String, Object> model = new HashMap<String, Object>();
        amplifyModel(obj, model);
        model.put(getParameterName(), wrapObject(obj));
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

    protected void amplifyModel(T t, Map<String, Object> model) {
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
