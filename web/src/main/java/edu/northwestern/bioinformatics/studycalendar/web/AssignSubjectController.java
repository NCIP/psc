package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Padmaja Vedula
 * @author Jalpa Patel
 */
public class AssignSubjectController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SubjectDao subjectDao;
    private SubjectService subjectService;
    private StudyDao studyDao;
    private StudySegmentDao studySegmentDao;
    private SiteDao siteDao;
    private PopulationDao populationDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private Configuration configuration;

    public AssignSubjectController() {
        setCommandClass(AssignSubjectCommand.class);
        setSuccessView("redirectToSchedule");
        setFormView("assignSubject");

        setBindOnNewForm(true);
        setCrumb(new Crumb());
        setValidator(new ValidatableValidator());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
        getControllerTools().registerDomainObjectEditor(binder, "site", siteDao);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        getControllerTools().registerDomainObjectEditor(binder, "subject", subjectDao);
        getControllerTools().registerDomainObjectEditor(binder, "populations", populationDao);
    }

    @Override
    protected Map<String, Object> referenceData(
        HttpServletRequest httpServletRequest, Object oCommand, Errors errors
    ) throws Exception {
        AssignSubjectCommand command = (AssignSubjectCommand) oCommand;
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Subject> subjects = subjectDao.getAll();
        Study study = command.getStudy();

        refdata.put("study", study);
        refdata.put("subjects", subjects);
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        List<StudySegment> studySegments = new ArrayList<StudySegment>();
        for(Epoch epoch:epochs) {
            getControllerTools().addHierarchyToModel(epoch, refdata);
            List<StudySegment> tempStudySegments = epoch.getStudySegments();
            for(StudySegment studySegment:tempStudySegments) {
                studySegments.add(studySegment);
            }
        }
        if (studySegments.size() > 1) {
            refdata.put("studySegments", studySegments);
        } else {
            refdata.put("studySegments", Collections.emptyList());
        }
        StudySite studySite = command.getStudySite();
        PscUser user = applicationSecurityManager.getUser();
        if (user != null && studySite != null) {
            UserStudySiteRelationship ussr = new UserStudySiteRelationship(user, studySite, configuration);
            refdata.put("canCreateSubjects", ussr.getCanCreateSubjects());
        }
        refdata.put("populations", study.getPopulations());

        Map<String, String> genders = Gender.getGenderMap();
        refdata.put("genders", genders);
        refdata.put("action", "New");
        refdata.put("site", command.getSite());
        return refdata;
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        if (!configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "UI subject creation is disabled.  There should be no links to this page visible.");
        }
        return super.showForm(request, response, errors);
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSubjectCommand command = (AssignSubjectCommand) oCommand;
        PscUser user = applicationSecurityManager.getUser();
        command.setStudySubjectCalendarManager(user);
        StudySubjectAssignment assignment = command.assignSubject();
        return new ModelAndView(getSuccessView(), "assignment", assignment.getId());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignSubjectCommand command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
        command.setSubjectDao(subjectDao);
        command.setConfiguration(configuration);
        return command;
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Assign Subject");
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = createParameters("study", context.getStudy().getId().toString());
            if (context.getSite() != null) {
                params.put("site", context.getSite().getId().toString());
            }
            return params;
        }
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
