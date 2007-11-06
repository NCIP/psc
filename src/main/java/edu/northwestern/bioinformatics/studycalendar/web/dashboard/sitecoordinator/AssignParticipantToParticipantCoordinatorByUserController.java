package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class AssignParticipantToParticipantCoordinatorByUserController extends PscSimpleFormController {
    private UserDao userDao;
    private StudySiteService studySiteService;
    private StudySiteDao studySiteDao;
    private UserService userService;
    private ParticipantDao participantDao;

    public AssignParticipantToParticipantCoordinatorByUserController() {
        setFormView("dashboard/sitecoordinator/assignParticipantToParticipantCoordinator");
        setSuccessView("studyList");
        setCommandClass(AssignParticipantToParticipantCoordinatorByUserCommand.class);
    }

    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();

        User siteCoordinator = getSiteCoordinator();

        Integer participantCoordinatorId = ServletRequestUtils.getIntParameter(request, "selected");

        Map<Site, Map<Study, List<Participant>>> displayMap = buildDisplayMap(participantCoordinatorId);
        Map<Study, Map<Site, List<User>>> studySiteParticipCoordMap = buildStudySiteParticipantCoordinatorMap(participantCoordinatorId);

        refData.put("displayMap", displayMap);
        refData.put("participantCoordinatorStudySites", studySiteParticipCoordMap);
        refData.put("assignableUsers", userService.getAssignableUsers(siteCoordinator));
        
        return refData;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
    }

    protected User getSiteCoordinator() {
        return userDao.getByName(ApplicationSecurityManager.getUser());
    }
                                                                                                                
    protected Map<Site, Map<Study, List<Participant>>> buildDisplayMap(Integer participantCoordinatorId) {
        Map<Site, Map<Study, List<Participant>>> displayMap = new TreeMap<Site, Map<Study, List<Participant>>>(new NamedComparator());

        if (participantCoordinatorId != null ) {

            User participantCoordinator = userDao.getById(participantCoordinatorId);
            if (participantCoordinator != null ) {

                List<StudySite> studySites = studySiteService.getAllStudySitesForParticipantCoordinator(participantCoordinator);

                for (StudySite studySite : studySites) {
                    Site site = studySite.getSite();
                    Study study = studySite.getStudy();

                    List<Participant> studyParticipants = new ArrayList<Participant>();
                    for (StudyParticipantAssignment assignment : studySite.getStudyParticipantAssignments()) {
                        Participant participant = assignment.getParticipant();
                        if (!assignment.isExpired()) {
                            studyParticipants.add(participant);
                        }
                    }

                    if (studyParticipants.size() > 0) {
                        if (!displayMap.containsKey(site)) {
                            displayMap.put(site, new TreeMap<Study, List<Participant>>(new NamedComparator()));
                        }

                        if (!displayMap.get(site).containsKey(study)) {
                            displayMap.get(site).put(study, new ArrayList<Participant>());
                        }

                        Collections.sort(displayMap.get(site).get(study), new Comparator<Participant>(){
                            public int compare(Participant participant, Participant participant1) {
                                return participant.getLastFirst().compareTo(participant1.getLastFirst());
                            }
                        });

                        displayMap.get(site).get(study).addAll(studyParticipants);
                    }
                }
            }
        }
        return displayMap;
    }

    protected Map<Study, Map<Site, List<User>>> buildStudySiteParticipantCoordinatorMap(Integer participantCoordinatorId) {
        Map<Study ,Map<Site, List<User>>> studySiteParticipantCoordinatorMap = new HashMap<Study ,Map<Site, List<User>>>();

        if (participantCoordinatorId != null ) {

            User participantCoordinator = userDao.getById(participantCoordinatorId);
            if (participantCoordinator != null ) {

                List<StudySite> studySites = studySiteService.getAllStudySitesForParticipantCoordinator(participantCoordinator);

                for (StudySite studySite : studySites) {
                    List<User> otherStudySiteParticipantCoords = new ArrayList<User>();
                    List<UserRole> userRoles = studySite.getUserRoles();
                    for (UserRole userRole : userRoles ) {
                        User user = userRole.getUser();
                        System.out.println(user.getName());
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
}
