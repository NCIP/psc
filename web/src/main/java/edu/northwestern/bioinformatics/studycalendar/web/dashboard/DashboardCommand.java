package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import org.apache.commons.collections15.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Rhett Sutphin
 */
public class DashboardCommand {
    private final PscUser loggedInUser;
    private PscUser dashboardUser;

    private final PscUserService pscUserService;
    private final StudyDao studyDao;

    private Map<Study, List<UserStudySiteRelationship>> assignableStudies;
    private List<PscUser> colleagues;

    public DashboardCommand(PscUser loggedInUser, PscUserService pscUserService, StudyDao studyDao) {
        this.loggedInUser = loggedInUser;
        this.dashboardUser = loggedInUser; // default if nothing bound

        this.pscUserService = pscUserService;
        this.studyDao = studyDao;
    }

    public boolean isColleagueDashboard() {
        return loggedInUser != getUser();
    }

    public boolean getHasHiddenInformation() {
        if (!isColleagueDashboard()) {
            return false;
        } else if (dashboardUser.getMembership(STUDY_SUBJECT_CALENDAR_MANAGER) == null) {
            return false;
        } else if (loggedInUser.getMembership(STUDY_SUBJECT_CALENDAR_MANAGER) == null) {
            return true;
        }

        Collection<Integer> possibleDashboardStudies = studyDao.getVisibleStudyIds(
            getUser().getVisibleStudyParameters(STUDY_SUBJECT_CALENDAR_MANAGER));
        Collection<Integer> visibleStudies = studyDao.getVisibleStudyIds(
            loggedInUser.getVisibleStudyParameters(STUDY_SUBJECT_CALENDAR_MANAGER));
        if (!CollectionUtils.isSubCollection(possibleDashboardStudies, visibleStudies)) {
            return true;
        }

        Collection<Integer> possibleDashboardAssignments = pscUserService.getVisibleAssignmentIds(
            getUser(), STUDY_SUBJECT_CALENDAR_MANAGER);
        Collection<Integer> visibleAssignments = pscUserService.getVisibleAssignmentIds(
            loggedInUser, STUDY_SUBJECT_CALENDAR_MANAGER);

        return !CollectionUtils.isSubCollection(possibleDashboardAssignments, visibleAssignments);
    }

    public Map<Study, List<UserStudySiteRelationship>> getAssignableStudies() {
        if (assignableStudies == null) {
            List<Study> studies = studyDao.getVisibleStudies(getUser().
                getVisibleStudyParameters(STUDY_SUBJECT_CALENDAR_MANAGER));
            assignableStudies = new LinkedHashMap<Study, List<UserStudySiteRelationship>>();
            for (Study study : studies) {
                for (StudySite studySite : study.getStudySites()) {
                    UserStudySiteRelationship loggedInRel =
                        new UserStudySiteRelationship(loggedInUser, studySite);
                    UserStudySiteRelationship dashboardRel =
                        new UserStudySiteRelationship(getUser(), studySite);
                    if (loggedInRel.getCanAssignSubjects() && dashboardRel.getCanAssignSubjects()) {
                        if (!assignableStudies.containsKey(study)) {
                            assignableStudies.put(study, new ArrayList<UserStudySiteRelationship>());
                        }
                        assignableStudies.get(study).add(loggedInRel);
                    }
                }
            }
        }
        return assignableStudies;
    }

    public List<PscUser> getColleagues() {
        if (colleagues == null) {
            colleagues = pscUserService.getColleaguesOf(
                loggedInUser, STUDY_SUBJECT_CALENDAR_MANAGER);
            for (Iterator<PscUser> it = colleagues.iterator(); it.hasNext();) {
                PscUser user = it.next();
                if (user.getUsername().equals(loggedInUser.getUsername())) it.remove();
            }
        }
        return colleagues;
    }

    ////// BOUND PROPERTIES

    public PscUser getUser() {
        return this.dashboardUser;
    }

    public void setUser(PscUser colleague) {
        this.dashboardUser = colleague;
    }
}
