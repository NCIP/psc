package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class AssignParticipantToParticipantCoordinatorByUserController extends PscSimpleFormController {
    private UserDao userDao;
    private StudySiteService studySiteService;

    public AssignParticipantToParticipantCoordinatorByUserController() {
        setFormView("siteCoordinatorDashboard");
    }

    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();
        Integer participantCoordinatorId = ServletRequestUtils.getIntParameter(request, "selected");
        User participantCoordinator = userDao.getById(participantCoordinatorId);
        List<StudySite> studySites = studySiteService.getAllStudySitesForParticipantCoordinator(participantCoordinator);
        return refData;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }
}
