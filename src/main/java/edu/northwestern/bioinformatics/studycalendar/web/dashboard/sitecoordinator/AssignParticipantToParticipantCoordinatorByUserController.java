package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class AssignParticipantToParticipantCoordinatorByUserController extends PscSimpleFormController {
    private UserDao userDao;
    private StudySiteService studySiteService;

    public AssignParticipantToParticipantCoordinatorByUserController() {
        setFormView("dashboard/sitecoordinator/siteCoordinatorDashboard");
        setSuccessView("studyList");
        setCommandClass(AssignParticipantToParticipantCoordinatorByUserCommand.class);
    }

    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();
        Integer participantCoordinatorId = ServletRequestUtils.getIntParameter(request, "selected");
        User participantCoordinator = userDao.getById(participantCoordinatorId);
        List<StudySite> studySites = studySiteService.getAllStudySitesForParticipantCoordinator(participantCoordinator);

        Map<Site, Map<Study, List<Participant>>> displayMap = new TreeMap<Site, Map<Study, List<Participant>>>(new NamedComparator());
        for (StudySite studySite : studySites) {
            Site site = studySite.getSite();
            if (!displayMap.containsKey(site)) {
                displayMap.put(site, new TreeMap<Study, List<Participant>>(new NamedComparator()));
            }

            Study study = studySite.getStudy();
            if (!displayMap.get(site).containsKey(study)) {
                displayMap.get(site).put(study, new ArrayList<Participant>());
            }

            for (StudyParticipantAssignment assignment : studySite.getStudyParticipantAssignments()) {
                Participant participant = assignment.getParticipant();
                if (!assignment.isExpired()) {
                    displayMap.get(site).get(study).add(participant);
                    Collections.sort(displayMap.get(site).get(study), new Comparator<Participant>(){

                        public int compare(Participant participant, Participant participant1) {
                            return participant.getLastName().compareTo(participant1.getLastName());
                        }
                    });
                }
            }
        }

        refData.put("displayMap", displayMap);
        
        return refData;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }
}
