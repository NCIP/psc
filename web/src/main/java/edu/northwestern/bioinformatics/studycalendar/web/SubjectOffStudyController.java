package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class SubjectOffStudyController extends PscSimpleFormController {
    private SubjectService subjectService;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    public SubjectOffStudyController(){
        setCommandClass(SubjectOffStudyCommand.class);
        setFormView("subjectOffStudy");
        setSuccessView("redirectToSchedule");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        SubjectOffStudyCommand command = (SubjectOffStudyCommand) oCommand;
        StudySubjectAssignment assignment = command.takeSubjectOffStudy();
        return new ModelAndView(getSuccessView(), new ModelMap("calendar", assignment.getScheduledCalendar().getId().toString()));
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(false));
        getControllerTools().registerDomainObjectEditor(binder, "assignment", studySubjectAssignmentDao);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SubjectOffStudyCommand command = new SubjectOffStudyCommand();
        command.setSubjectService(subjectService);
        return command;
    }

    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        getControllerTools().addHierarchyToModel(((SubjectOffStudyCommand) oCommand).getAssignment(), model);
        return model;
    }

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public void setStudySubjectAssignmentDao (StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

     private static class Crumb extends DefaultCrumb {
        public String getName(DomainContext context) {
            StudySubjectAssignment assignment = context.getStudySubjectAssignment();
            return new StringBuilder()
                    .append("Take ")
                    .append(assignment.getSubject().getFullName())
                    .append(" off study").toString();
        }

        public Map<String, String> getParameters(DomainContext context) {
            return createParameters("assignment", context.getStudySubjectAssignment().getId().toString());
        }
    }

}
