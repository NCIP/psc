package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.LinkedList;
import java.util.TreeMap;

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
    private PopulationDao populationDao;

    public AssignSubjectController() {
        setCommandClass(AssignSubjectCommand.class);
        setFormView("assignSubject");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
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

        addAvailableSitesRefdata(refdata, study);
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
        refdata.put("populations", study.getPopulations());
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSubjectCommand command = (AssignSubjectCommand) oCommand;
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        command.setSubjectCoordinator(user);
        StudySubjectAssignment assignment = command.assignSubject();
        return new ModelAndView("redirectToSchedule", "assignment", assignment.getId());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignSubjectCommand command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
        return command;
    }

    private void addAvailableSitesRefdata(Map<String, Object> refdata, Study study) {
        UserRole subjCoord = userDao.getByName(ApplicationSecurityManager.getUser()).getUserRole(Role.SUBJECT_COORDINATOR);
        List<StudySite> applicableStudySites = new LinkedList<StudySite>();
        for (StudySite studySite : study.getStudySites()) {
            if (subjCoord.getStudySites().contains(studySite)) applicableStudySites.add(studySite);
        }
        Map<Site, String> sites = new TreeMap<Site, String>(NamedComparator.INSTANCE);
        SortedSet<Site> unapproved = new TreeSet<Site>(NamedComparator.INSTANCE);
        for (StudySite studySite : applicableStudySites) {
            Site site = studySite.getSite();
            Amendment currentApproved = studySite.getCurrentApprovedAmendment();
            if (currentApproved == null) {
                log.debug("{} has not approved any amendments for {}", site.getName(), study.getName());
                unapproved.add(site);
            } else {
                log.debug("{} has approved up to {}", site.getName(), currentApproved);
                StringBuilder title = new StringBuilder(site.getName());
                if (!currentApproved.isFirst()) {
                    title.append(" - amendment ").append(currentApproved.getDisplayName());
                    if (study.getAmendment().equals(currentApproved)) {
                        title.append(" (current)");
                    }
                }
                sites.put(site, title.toString());
            }
        }

        refdata.put("sites", sites);
        refdata.put("unapprovedSites", unapproved);
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
    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
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

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            Map<String, String> params = createParameters("study", context.getStudy().getId().toString());
            if (context.getSite() != null) {
                params.put("site", context.getSite().getId().toString());
            }
            return params;
        }
    }
}
