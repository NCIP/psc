package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Rhett Sutphin
 */
public class ChangeAmendmentController extends PscSimpleFormController implements PscAuthorizedHandler {
    private AmendmentDao amendmentDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private DeltaService deltaService;

    public ChangeAmendmentController() {
        setCommandClass(ChangeAmendmentCommand.class);
        setFormView("schedule/changeAmendment");
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studySubjectAssignmentArray = queryParameters.get("assignment");
        try {
            String studySubjectAssignmentString = studySubjectAssignmentArray[0];
            Integer studySubjectAssignmentId = Integer.parseInt(studySubjectAssignmentString);
            StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getById(studySubjectAssignmentId);
            StudySite studySite = studySubjectAssignment.getStudySite();
            Site site = studySite.getSite();
            return ResourceAuthorization.createCollection(site, STUDY_SUBJECT_CALENDAR_MANAGER);
        } catch (Exception e) {
            return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
        }
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

        public Map<String, String> getParameters(DomainContext context) {
            return createParameters("assignment", context.getStudySubjectAssignment().getId().toString());
        }
    }
}
