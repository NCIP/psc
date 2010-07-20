package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class DeleteDevelopmentAmendmentController extends PscSimpleFormController implements PscAuthorizedHandler {

    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private TemplateDevelopmentService templateDevelopmentService;

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    public DeleteDevelopmentAmendmentController() {
        setCommandClass(DeleteDevelopmentAmendmentCommand.class);
        setFormView("template/deleteDevelopmentAmendment");
        setCrumb(new Crumb());

    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
    }

    @Override
    protected Map referenceData(final HttpServletRequest request, final Object oCommand, final Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap();
        // include study in refdata for breadcrumbs
        DeleteDevelopmentAmendmentCommand command = ((DeleteDevelopmentAmendmentCommand) oCommand);
        getControllerTools().addHierarchyToModel(command.getStudy(), refdata);
        return refdata;

    }

    @Override
    protected Object formBackingObject(final HttpServletRequest request) throws Exception {
        DeleteDevelopmentAmendmentCommand command = new DeleteDevelopmentAmendmentCommand(amendmentService, templateDevelopmentService);
        return command;
    }

    @Override
    protected ModelAndView onSubmit(final HttpServletRequest request, final HttpServletResponse httpServletResponse, final Object o, final BindException e) throws Exception {
        DeleteDevelopmentAmendmentCommand command = (DeleteDevelopmentAmendmentCommand) o;
        if (!command.getStudy().isInDevelopment()) {
            e.reject("A released template can not be deleted.");
            return showForm(request, httpServletResponse, e);
        } else {
            command.apply();
            return new ModelAndView("redirectToStudyList");
        }


    }


    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            StringBuilder sb = new StringBuilder("Delete " + context.getStudy().getName());
            return sb.toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("study", context.getStudy().getId().toString());
            return params;
        }
    }


    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setTemplateDevelopmentService(TemplateDevelopmentService templateDevelopmentService) {
        this.templateDevelopmentService = templateDevelopmentService;
    }
}
