/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class StudyTeamController extends PscAbstractController implements PscAuthorizedHandler {
    private ApplicationSecurityManager applicationSecurityManager;
    private PscUserService pscUserService;
    private StudyDao studyDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        return ResourceAuthorization.createCollection(PscRole.STUDY_TEAM_ADMINISTRATOR);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PscUser teamAdmin = applicationSecurityManager.getUser();
        ModelAndView mv = new ModelAndView("admin/studyTeam");
        mv.addObject("teamJSON", buildTeam(teamAdmin).toString(4));
        mv.addObject("studiesJSON", visibleStudies(teamAdmin).toString(4));
        mv.addObject("unclaimedSubjectsCount",
            studySubjectAssignmentDao.getAssignmentsWithoutManagerCsmUserId().size());
        return mv;
    }

    private JSONArray buildTeam(PscUser teamAdmin) {
        Map<Integer, Long> managerIds = studySubjectAssignmentDao.getManagerCsmUserIdCounts();
        Collection<PscUser> teamUsers = pscUserService.getTeamMembersFor(teamAdmin);
        JSONArray team = new JSONArray();
        for (PscUser user : teamUsers) {
            team.put(new TeamMember(user,
                managerIds.get(user.getCsmUser().getUserId().intValue())).toJSON());
        }
        return team;
    }

    private JSONArray visibleStudies(PscUser teamAdmin) {
        List<Study> visibleStudies =
            studyDao.getVisibleStudies(teamAdmin.getVisibleStudyParameters(PscRole.STUDY_TEAM_ADMINISTRATOR));
        Collections.sort(visibleStudies, NamedComparator.INSTANCE);
        JSONArray json = new JSONArray();
        for (Study study : visibleStudies) {
            json.put(new JSONObject(new MapBuilder<String, Object>().
                put("id", study.getId()).
                put("identifier", study.getAssignedIdentifier()).
                toMap()));
        }
        return json;
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    //////

    public static class TeamMember {
        private PscUser user;
        private Number managedCalendarCount;

        public TeamMember(PscUser user, Number managedCalendarCount) {
            this.user = user;
            this.managedCalendarCount = managedCalendarCount;
        }

        public boolean isStudySubjectCalendarManager() {
            return this.user.getMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER) != null;
        }

        public Number getManagedCalendarCount() {
            return managedCalendarCount;
        }

        public JSONObject toJSON() {
            return new JSONObject(new MapBuilder<String, Object>().
                put("last_first", user.getLastFirst()).
                put("username", user.getUsername()).
                put("is_sscm", isStudySubjectCalendarManager()).
                put("managed_calendar_count", getManagedCalendarCount()).
                toMap());
        }
    }
}
