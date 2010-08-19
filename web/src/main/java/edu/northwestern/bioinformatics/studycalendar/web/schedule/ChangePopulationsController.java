package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Rhett Sutphin
 */
public class ChangePopulationsController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SubjectService subjectService;
    private StudySubjectAssignmentDao assignmentDao;
    private PopulationDao populationDao;


    protected ChangePopulationsController() {
        setCrumb(new Crumb());
        setCommandClass(ChangePopulationsCommand.class);
        setFormView("schedule/changePopulations");
    }
    
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studySubjectAssignmentArray = queryParameters.get("assignment");
        try {
            String studySubjectAssignmentString = studySubjectAssignmentArray[0];
            Integer studySubjectAssignmentId = Integer.parseInt(studySubjectAssignmentString);
            StudySubjectAssignment studySubjectAssignment = assignmentDao.getById(studySubjectAssignmentId);
            StudySite studySite = studySubjectAssignment.getStudySite();
            Site site = studySite.getSite();
            return ResourceAuthorization.createCollection(site, STUDY_SUBJECT_CALENDAR_MANAGER);
        } catch (Exception e) {
            return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
        }
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        StudySubjectAssignment assignment = assignmentDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "assignment"));
        return new ChangePopulationsCommand(assignment, subjectService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "assignment", assignmentDao);
        getControllerTools().registerDomainObjectEditor(binder, "populations", populationDao);
    }

    @Override
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        ChangePopulationsCommand command = (ChangePopulationsCommand) oCommand;
        Map<String, Object> refdata = new HashMap<String, Object>();
        getControllerTools().addHierarchyToModel(command.getAssignment(), refdata);
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ChangePopulationsCommand command = ((ChangePopulationsCommand) oCommand);
        command.apply();
        return getControllerTools().redirectToSchedule(command.getAssignment().getId());
    }

    ///// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setAssignmentDao(StudySubjectAssignmentDao assignmentDao) {
        this.assignmentDao = assignmentDao;
    }

    @Required
    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            return new StringBuilder()
                .append("Population")
                .toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = createParameters(
                "assignment", context.getStudySubjectAssignment().getId().toString()
            );
            return params;
        }
    }

}
