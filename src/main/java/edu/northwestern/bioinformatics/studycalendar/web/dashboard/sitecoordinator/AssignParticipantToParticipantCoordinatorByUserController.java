package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
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
public class AssignParticipantToParticipantCoordinatorByUserController extends PscSimpleFormController {
    private UserDao userDao;
    private StudySiteService studySiteService;
    private StudySiteDao studySiteDao;
    private UserService userService;
    private ParticipantDao participantDao;
    private StudyDao studyDao;
    private SiteDao siteDao;

    public AssignParticipantToParticipantCoordinatorByUserController() {
        setFormView("dashboard/sitecoordinator/assignParticipantToParticipantCoordinator");
        setSuccessView("studyList");
    }

    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();

        User siteCoordinator = getSiteCoordinator();

        Integer participantCoordinatorId = ServletRequestUtils.getIntParameter(request, "selected");
        User participantCoordinator = null;
        if (participantCoordinatorId != null ) {
            participantCoordinator = userDao.getById(participantCoordinatorId);
        }

        Map<Site, Map<Study, List<Participant>>> displayMap = buildDisplayMap(participantCoordinator);
        Map<Study, Map<Site, List<User>>> studySiteParticipCoordMap = buildStudySiteParticipantCoordinatorMap(participantCoordinator);

        refData.put("displayMap", displayMap);
        refData.put("participantCoordinatorStudySites", studySiteParticipCoordMap);
        refData.put("assignableUsers", userService.getSiteCoordinatorsAssignableUsers(siteCoordinator));
        refData.put("selectedId", participantCoordinatorId);
        
        return refData;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
        binder.registerCustomEditor(Site.class, "site", new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(Study.class, "study", new DaoBasedEditor(studyDao));
        binder.registerCustomEditor(Participant.class, "participantCoordinator", new DaoBasedEditor(participantDao));
        binder.registerCustomEditor(Participant.class, "participants", new DaoBasedEditor(participantDao));
    }


    protected ModelAndView onSubmit(Object o) throws Exception {
        AssignParticipantToParticipantCoordinatorByUserCommand command = (AssignParticipantToParticipantCoordinatorByUserCommand) o;

        command.assignParticipantsToParticipantCoordinator();
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("study", command.getStudy());
        model.put("site", command.getSite());
        model.put("participants", buildParticipants(findStudySite(command.getStudy(), command.getSite()), null));

        return new ModelAndView("dashboard/sitecoordinator/ajax/displayParticipants", model);
    }


    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignParticipantToParticipantCoordinatorByUserCommand command = new AssignParticipantToParticipantCoordinatorByUserCommand();
        command.setParticipantDao(participantDao);
        return command;
    }

    protected User getSiteCoordinator() {
        return userDao.getByName(ApplicationSecurityManager.getUser());
    }
                                                                                                                
    protected Map<Site, Map<Study, List<Participant>>> buildDisplayMap(User participantCoordinator) {
        Map<Site, Map<Study, List<Participant>>> displayMap = new TreeMap<Site, Map<Study, List<Participant>>>(new NamedComparator());


            if (participantCoordinator != null ) {

                List<StudySite> studySites = studySiteService.getAllStudySitesForParticipantCoordinator(participantCoordinator);

                for (StudySite studySite : studySites) {
                    Site site = studySite.getSite();
                    Study study = studySite.getStudy();

                    List<Participant> studyParticipants = buildParticipants(studySite, participantCoordinator);

                    if (studyParticipants.size() > 0) {
                        if (!displayMap.containsKey(site)) {
                            displayMap.put(site, new TreeMap<Study, List<Participant>>(new NamedComparator()));
                        }

                        if (!displayMap.get(site).containsKey(study)) {
                            displayMap.get(site).put(study, new ArrayList<Participant>());
                        }

                        displayMap.get(site).get(study).addAll(studyParticipants);
                    }
                }
            }
        return displayMap;
    }

    protected List<Participant> buildParticipants(StudySite studySite, User participantCoordinator) {
        List<Participant> studyParticipants = new ArrayList<Participant>();
        if (studySite != null ) {
            for (StudyParticipantAssignment assignment : studySite.getStudyParticipantAssignments()) {
                Participant participant = assignment.getParticipant();
                if (assignment.getParticipantCoordinator().equals(participantCoordinator) && !assignment.isExpired()) {
                    studyParticipants.add(participant);
                }
            }
        }

        Collections.sort(studyParticipants, new Comparator<Participant>(){
            public int compare(Participant participant, Participant participant1) {
                return participant.getLastFirst().compareTo(participant1.getLastFirst());
            }
        });
        return studyParticipants;
    }

    protected Map<Study, Map<Site, List<User>>> buildStudySiteParticipantCoordinatorMap(User participantCoordinator) {
        Map<Study ,Map<Site, List<User>>> studySiteParticipantCoordinatorMap = new HashMap<Study ,Map<Site, List<User>>>();

            if (participantCoordinator != null ) {

                List<StudySite> studySites = studySiteService.getAllStudySitesForParticipantCoordinator(participantCoordinator);

                for (StudySite studySite : studySites) {
                    List<User> otherStudySiteParticipantCoords = new ArrayList<User>();
                    List<UserRole> userRoles = studySite.getUserRoles();
                    for (UserRole userRole : userRoles ) {
                        User user = userRole.getUser();
                        if (!user.equals(participantCoordinator)) {
                            otherStudySiteParticipantCoords.add(user);
                        }
                    }
                    Collections.sort(otherStudySiteParticipantCoords, new NamedComparator());
                    if (!studySiteParticipantCoordinatorMap.containsKey(studySite.getStudy())) {
                        studySiteParticipantCoordinatorMap.put(studySite.getStudy(), new HashMap<Site, List<User>>());
                    }
                    studySiteParticipantCoordinatorMap.get(studySite.getStudy()).put(studySite.getSite(), otherStudySiteParticipantCoords);
                }
            }
        return studySiteParticipantCoordinatorMap;
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

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
