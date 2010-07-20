package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SUBJECT_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Padmaja Vedula
 * @author Jalpa Patel
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class AssignSubjectController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SubjectDao subjectDao;
    private SubjectService subjectService;
    private StudyDao studyDao;
    private StudySegmentDao studySegmentDao;
    private SiteDao siteDao;
    private PopulationDao populationDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private String radioButton;

    public AssignSubjectController() {
        setCommandClass(AssignSubjectCommand.class);
        setSuccessView("redirectToSchedule");
        setFormView("assignSubject");

        setBindOnNewForm(true);
        setCrumb(new Crumb());
        setValidator(new ValidatableValidator());
    }


    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        if (getRadioButton() == null) {
            return ResourceAuthorization.createCollection(SUBJECT_MANAGER);
        } else {
            if (httpMethod.toLowerCase().equals("post")) {
                if (getRadioButton().equals("new")) {
                    return ResourceAuthorization.createCollection(SUBJECT_MANAGER);
                }
                else {
                    return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
                }
            } else {
                return ResourceAuthorization.createCollection(SUBJECT_MANAGER);
            }
        }
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
        setRadioButton(ServletRequestUtils.getStringParameter(request, "radioButton"));
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
        refdata.put("populations", study.getPopulations());

        Map<String, String> genders = Gender.getGenderMap();
        refdata.put("genders", genders);
        refdata.put("action", "New");
        refdata.put("defaultSite",httpServletRequest.getParameter("site"));
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSubjectCommand command = (AssignSubjectCommand) oCommand;
        User user = applicationSecurityManager.getUser().getLegacyUser();
        command.setSubjectCoordinator(user);
        StudySubjectAssignment assignment = command.assignSubject();
        return new ModelAndView(getSuccessView(), "assignment", assignment.getId());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignSubjectCommand command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
        command.setSubjectDao(subjectDao);
        return command;
    }

    private void addAvailableSitesRefdata(Map<String, Object> refdata, Study study) {
        UserRole subjCoord = applicationSecurityManager.getUser().getLegacyUser().getUserRole(Role.SUBJECT_COORDINATOR);
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


    public String getRadioButton() {
        return radioButton;
    }

    public void setRadioButton(String radioButton) {
        this.radioButton = radioButton;
    }

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
}
