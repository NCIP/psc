package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class AssignSubjectToSubjectCoordinatorByUserController extends PscSimpleFormController {
    private UserDao userDao;
    private StudySiteService studySiteService;
    private StudySiteDao studySiteDao;
    private UserService userService;
    private SubjectDao subjectDao;
    private StudyDao studyDao;
    private SiteDao siteDao;

    public AssignSubjectToSubjectCoordinatorByUserController() {
        setFormView("dashboard/sitecoordinator/assignSubjectToSubjectCoordinator");
        setSuccessView("studyList");
    }

    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();

        User siteCoordinator = getSiteCoordinator();

        Integer subjectCoordinatorId = ServletRequestUtils.getIntParameter(request, "selected");
        User subjectCoordinator = null;
        if (subjectCoordinatorId != null ) {
            subjectCoordinator = userDao.getById(subjectCoordinatorId);
        }

        Map<Site, Map<Study, List<Subject>>> displayMap = buildDisplayMap(subjectCoordinator);
        Map<Study, Map<Site, List<User>>> studySiteParticipCoordMap = buildStudySiteSubjectCoordinatorMap(subjectCoordinator);

        refData.put("displayMap", displayMap);
        refData.put("subjectCoordinatorStudySites", studySiteParticipCoordMap);
        refData.put("assignableUsers", userService.getSiteCoordinatorsAssignableUsers(siteCoordinator));
        refData.put("selectedId", subjectCoordinatorId);
        
        return refData;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
        binder.registerCustomEditor(Site.class, "site", new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(Study.class, "study", new DaoBasedEditor(studyDao));
        binder.registerCustomEditor(Subject.class, "subjectCoordinator", new DaoBasedEditor(subjectDao));
        binder.registerCustomEditor(Subject.class, "subjects", new DaoBasedEditor(subjectDao));
    }


    protected ModelAndView onSubmit(Object o) throws Exception {
        AssignSubjectToSubjectCoordinatorByUserCommand command = (AssignSubjectToSubjectCoordinatorByUserCommand) o;

        command.assignSubjectsToSubjectCoordinator();
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("study", command.getStudy());
        model.put("site", command.getSite());
        model.put("subjects", buildSubjects(findStudySite(command.getStudy(), command.getSite()), command.getSelected()));

        return new ModelAndView("dashboard/sitecoordinator/ajax/displaySubjects", model);
    }


    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignSubjectToSubjectCoordinatorByUserCommand command = new AssignSubjectToSubjectCoordinatorByUserCommand();
        command.setSubjectDao(subjectDao);
        return command;
    }

    protected User getSiteCoordinator() {
        return userDao.getByName(ApplicationSecurityManager.getUserName());
    }
                                                                                                                
    protected Map<Site, Map<Study, List<Subject>>> buildDisplayMap(User subjectCoordinator) {
        Map<Site, Map<Study, List<Subject>>> displayMap = new TreeMap<Site, Map<Study, List<Subject>>>(new NamedComparator());


            if (subjectCoordinator != null ) {

                List<StudySite> studySites = studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoordinator);

                for (StudySite studySite : studySites) {
                    Site site = studySite.getSite();
                    Study study = studySite.getStudy();

                    List<Subject> studySubjects = buildSubjects(studySite, subjectCoordinator);

                    if (studySubjects.size() > 0) {
                        if (!displayMap.containsKey(site)) {
                            displayMap.put(site, new TreeMap<Study, List<Subject>>(new NamedComparator()));
                        }

                        if (!displayMap.get(site).containsKey(study)) {
                            displayMap.get(site).put(study, new ArrayList<Subject>());
                        }

                        displayMap.get(site).get(study).addAll(studySubjects);
                    }
                }
            }
        return displayMap;
    }

    protected List<Subject> buildSubjects(StudySite studySite, User subjectCoordinator) {
        List<Subject> studySubjects = new ArrayList<Subject>();
        if (studySite != null ) {
            for (StudySubjectAssignment assignment : studySite.getStudySubjectAssignments()) {
                Subject subject = assignment.getSubject();
                if (assignment.getSubjectCoordinator().equals(subjectCoordinator) && !assignment.isExpired()) {
                    studySubjects.add(subject);
                }
            }
        }

        Collections.sort(studySubjects, new Comparator<Subject>(){
            public int compare(Subject subject, Subject subject1) {
                return subject.getLastFirst().compareTo(subject1.getLastFirst());
            }
        });
        return studySubjects;
    }

    protected Map<Study, Map<Site, List<User>>> buildStudySiteSubjectCoordinatorMap(User subjectCoordinator) {
        Map<Study ,Map<Site, List<User>>> studySiteSubjectCoordinatorMap = new HashMap<Study ,Map<Site, List<User>>>();

            if (subjectCoordinator != null ) {

                List<StudySite> studySites = studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoordinator);

                for (StudySite studySite : studySites) {
                    List<User> otherStudySiteSubjectCoords = new ArrayList<User>();
                    List<UserRole> userRoles = studySite.getUserRoles();
                    for (UserRole userRole : userRoles ) {
                        User user = userRole.getUser();
                        if (!user.equals(subjectCoordinator)) {
                            otherStudySiteSubjectCoords.add(user);
                        }
                    }
                    Collections.sort(otherStudySiteSubjectCoords, new NamedComparator());
                    if (!studySiteSubjectCoordinatorMap.containsKey(studySite.getStudy())) {
                        studySiteSubjectCoordinatorMap.put(studySite.getStudy(), new HashMap<Site, List<User>>());
                    }
                    studySiteSubjectCoordinatorMap.get(studySite.getStudy()).put(studySite.getSite(), otherStudySiteSubjectCoords);
                }
            }
        return studySiteSubjectCoordinatorMap;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }

    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    public void setUserService(UserService userService) {   
        this.userService = userService;
    }

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
