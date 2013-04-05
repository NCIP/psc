/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

public class SubjectOffStudyController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SubjectService subjectService;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    public SubjectOffStudyController(){
        setCommandClass(SubjectOffStudyCommand.class);
        setFormView("subjectOffStudy");
        setSuccessView("redirectToSchedule");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
        setValidator(new ValidatableValidator());
    }

    @Override
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] assignmentArray = queryParameters.get("assignment");
        try {
            String assignmentString = assignmentArray[0];
            Integer assignmentId = Integer.parseInt(assignmentString);
            StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getById(assignmentId);
            StudySite studySite = studySubjectAssignment.getStudySite();
            Site site = studySite.getSite();
            Study study = studySite.getStudy();
            return ResourceAuthorization.createCollection(site, study, STUDY_SUBJECT_CALENDAR_MANAGER);
        } catch (Exception e) {
            return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
        }
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
