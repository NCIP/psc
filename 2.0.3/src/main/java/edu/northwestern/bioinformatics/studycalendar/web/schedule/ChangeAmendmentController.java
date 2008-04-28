package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ChangeAmendmentController extends PscSimpleFormController {
    private AmendmentDao amendmentDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private DeltaService deltaService;

    public ChangeAmendmentController() {
        setCommandClass(ChangeAmendmentCommand.class);
        setFormView("schedule/changeAmendment");
        setCrumb(new Crumb());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ChangeAmendmentCommand(
            studySubjectAssignmentDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "assignment")),
            deltaService
        );
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request,binder);
        getControllerTools().registerDomainObjectEditor(binder, null, amendmentDao);
    }

    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        ChangeAmendmentCommand command = (ChangeAmendmentCommand) oCommand;
        Map<String, Object> refdata = new HashMap<String, Object>();
        getControllerTools().addHierarchyToModel(command.getAssignment(), refdata);
        return refdata;
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ChangeAmendmentCommand command = (ChangeAmendmentCommand) oCommand;
        command.apply();
        return getControllerTools().redirectToSchedule(command.getAssignment().getId());
    }

    ////// CONFIGURATION

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Change amendment");
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return createParameters("assignment", context.getStudySubjectAssignment().getId().toString());
        }
    }
}
