package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Padmaja Vedula
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class AssignSubjectController extends PscSimpleFormController {
    private SubjectDao subjectDao;
    private SubjectService subjectService;
    private SiteService siteService;
    private StudyDao studyDao;
    private StudySegmentDao studySegmentDao;
    private UserDao userDao;
    private SiteDao siteDao;


    public AssignSubjectController() {
        setCommandClass(AssignSubjectCommand.class);
        setFormView("assignSubject");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
        getControllerTools().registerDomainObjectEditor(binder, "site", siteDao);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        getControllerTools().registerDomainObjectEditor(binder, "subject", subjectDao);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Subject> subjects = subjectDao.getAll();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));

        refdata.put("sites", getAvailableSites(study));
        refdata.put("study", study);
        refdata.put("subjects", subjects);
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        getControllerTools().addHierarchyToModel(epoch, refdata);
        List<StudySegment> studySegments = epoch.getStudySegments();
        if (studySegments.size() > 1) {
            refdata.put("studySegments", studySegments);
        } else {
            refdata.put("studySegments", Collections.emptyList());
        }
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSubjectCommand command = (AssignSubjectCommand) oCommand;
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        command.setSubjectCoordinator(user);
        StudySubjectAssignment assignment = command.assignSubject();
        return new ModelAndView("redirectToSchedule", "assignment", assignment.getId().intValue());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignSubjectCommand command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);

        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "id"));
        Integer siteId = ServletRequestUtils.getIntParameter(request, "siteId");

        List<Site> availableSites = getAvailableSites(study);
        Site defaultSite  = (siteId != null) ? siteDao.getById(siteId) : availableSites.get(0);
        command.setSite(defaultSite);

        return command;
    }

    private List<Site> getAvailableSites(Study study) {
        String userName = ApplicationSecurityManager.getUser();
        Collection<Site> availableSites = siteService.getSitesForSubjectCoordinator(userName, study);
        return new ArrayList<Site>(availableSites);
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
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Assign Subject");
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return createParameters("id", context.getStudy().getId().toString());
        }
    }
}
