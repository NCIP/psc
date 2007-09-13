package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ParticipantOffStudyController extends PscSimpleFormController {
    private ParticipantService participantService;
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;

    public ParticipantOffStudyController(){
        setCommandClass(ParticipantOffStudyCommand.class);
        setFormView("participantOffStudy");
        setSuccessView("redirectToSchedule");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ParticipantOffStudyCommand command = (ParticipantOffStudyCommand) oCommand;
        StudyParticipantAssignment assignment = command.takeParticipantOffStudy();
        return new ModelAndView(getSuccessView(), new ModelMap("calendar", assignment.getScheduledCalendar().getId().toString()));
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(false));
        getControllerTools().registerDomainObjectEditor(binder, "assignment", studyParticipantAssignmentDao);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ParticipantOffStudyCommand command = new ParticipantOffStudyCommand();
        command.setParticipantService(participantService);
        return command;
    }

    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        getControllerTools().addHierarchyToModel(((ParticipantOffStudyCommand) oCommand).getAssignment(), model);
        return model;
    }

    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }

    public void setStudyParticipantAssignmentDao(StudyParticipantAssignmentDao studyParticipantAssignmentDao) {
        this.studyParticipantAssignmentDao = studyParticipantAssignmentDao;
    }

     private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            StudyParticipantAssignment assignment = context.getStudyParticipantAssignment();
            return new StringBuilder()
                    .append("Take ")
                    .append(assignment.getParticipant().getFullName())
                    .append(" off study").toString();
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return createParameters("assignment", context.getStudyParticipantAssignment().getId().toString());
        }
    }

}
