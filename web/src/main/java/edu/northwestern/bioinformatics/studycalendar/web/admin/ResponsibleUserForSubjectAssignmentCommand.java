/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class ResponsibleUserForSubjectAssignmentCommand implements Validatable {
    private final PscUserService pscUserService;
    private StudySubjectAssignmentDao assignmentDao;

    private final PscUser teamAdmin;
    private List<PscUser> team;
    private Map<Site, Map<Study, ReassignableAssignments>> reassignables;

    private PscUser responsible, newResponsible;
    private List<StudySubjectAssignment> targetAssignments;
    private Configuration configuration;

    public ResponsibleUserForSubjectAssignmentCommand(
        PscUser teamAdmin, PscUserService pscUserService, StudySubjectAssignmentDao assignmentDao,
        Configuration configuration
    ) {
        this.teamAdmin = teamAdmin;
        this.pscUserService = pscUserService;
        this.assignmentDao = assignmentDao;
        this.configuration = configuration;
    }

    ///// LOGIC

    public void validate(Errors errors) {
        if (getTargetAssignments() == null || getTargetAssignments().isEmpty()) {
            errors.rejectValue("targetAssignments", "responsible-user.no-targets",
                "Please select at least one subject schedule to change.");
        } else {
            Collection<StudySubjectAssignment> eligibleTargets = getEligibleTargets();
            for (StudySubjectAssignment assignment : getTargetAssignments()) {
                if (!eligibleTargets.contains(assignment)) {
                    errors.rejectValue("targetAssignments", "responsible-user.forbidden-target",
                        new Object[] { assignment.getId() },
                        "You may not change the responsible user for the assignment with internal ID {0}.");
                }
            }
        }
        // skip user errors to avoid leaking PHI if they requested to modify an assignment they can't see.
        if (errors.hasErrors()) return;

        if (getNewResponsible() == null) {
            errors.rejectValue("newResponsible", "responsible-user.no-user",
                "Please select a new user to be responsible for these subject schedules.");
        } else {
            for (StudySubjectAssignment assignment : getTargetAssignments()) {
                UserStudySubjectAssignmentRelationship rel =
                    new UserStudySubjectAssignmentRelationship(getNewResponsible(), assignment);
                if (!rel.getCanUpdateSchedule()) {
                    errors.rejectValue("newResponsible", "responsible-user.ineligible-new-user",
                        new Object[] {
                            getNewResponsible().getDisplayName(),
                            assignment.getSubject().getFullName() },
                        "{0} is not eligible to be responsible for {1}");
                }
            }
        }
    }

    public void apply() {
        for (StudySubjectAssignment targetAssignment : targetAssignments) {
            targetAssignment.setStudySubjectCalendarManager(newResponsible.getCsmUser());
            assignmentDao.save(targetAssignment);
        }
    }

    ///// REFERENCE PROPERTIES

    public Map<Site, Map<Study, ReassignableAssignments>> getReassignables() {
        if (reassignables == null) {
            if (getResponsible() == null) {
                List<StudySubjectAssignment> unmanaged = assignmentDao.
                    getAssignmentsWithoutManagerCsmUserId();
                for (StudySubjectAssignment assignment : unmanaged) {
                    UserStudySubjectAssignmentRelationship teamAdminRel =
                        new UserStudySubjectAssignmentRelationship(teamAdmin, assignment);
                    if (!teamAdminRel.getCanSetCalendarManager()) continue;
                    pushReassignable(assignment, null, true); // things can stay unmanaged
                }
            } else {
                List<UserStudySubjectAssignmentRelationship> designated =
                    pscUserService.getDesignatedManagedAssignments(getResponsible());
                for (UserStudySubjectAssignmentRelationship ussar : designated) {
                    UserStudySubjectAssignmentRelationship teamAdminRel =
                        new UserStudySubjectAssignmentRelationship(teamAdmin, ussar.getAssignment());
                    if (!teamAdminRel.getCanSetCalendarManager()) continue;
                    pushReassignable(ussar);
                }
            }
        }
        return reassignables;
    }

    private void pushReassignable(UserStudySubjectAssignmentRelationship currentManagerRelationship) {
        pushReassignable(currentManagerRelationship.getAssignment(),
            currentManagerRelationship.getUser(), currentManagerRelationship.getCanUpdateSchedule());
    }

    private void pushReassignable(
        StudySubjectAssignment assignment, PscUser currentManager, boolean stillManageable
    ) {
        StudySite studySite = assignment.getStudySite();
        if (reassignables == null) {
            reassignables = new TreeMap<Site, Map<Study, ReassignableAssignments>>(NamedComparator.INSTANCE);
        }
        Site site = studySite.getSite();
        if (!reassignables.containsKey(site)) {
            reassignables.put(site,
                new TreeMap<Study, ReassignableAssignments>(NamedComparator.INSTANCE));
        }
        Study study = studySite.getStudy();
        if (!reassignables.get(site).containsKey(study)) {
            reassignables.get(site).put(study, new ReassignableAssignments(
                studySite, stillManageable, findTeamFor(studySite, currentManager)));
        }
        reassignables.get(site).get(study).getAssignments().add(assignment);
    }

    private Collection<StudySubjectAssignment> getEligibleTargets() {
        List<StudySubjectAssignment> eligible = new ArrayList<StudySubjectAssignment>();
        for (Map.Entry<Site, Map<Study, ReassignableAssignments>> siteMapEntry : getReassignables().entrySet()) {
            for (ReassignableAssignments assignments : siteMapEntry.getValue().values()) {
                eligible.addAll(assignments.getAssignments());
            }
        }
        return eligible;
    }

    private List<PscUser> getTeam() {
        if (team == null) {
            team = pscUserService.getColleaguesOf(
                teamAdmin, STUDY_SUBJECT_CALENDAR_MANAGER, STUDY_TEAM_ADMINISTRATOR);
        }
        return team;
    }

    private List<PscUser> findTeamFor(StudySite studySite, PscUser currentManager) {
        List<PscUser> subteam = new ArrayList<PscUser>(getTeam());
        for (Iterator<PscUser> it = subteam.iterator(); it.hasNext();) {
            PscUser candidate = it.next();
            if (!(new UserStudySiteRelationship(candidate, studySite, configuration)).getCanManageCalendars()) {
                it.remove();
            } else if (currentManager != null && candidate.getUsername().equals(currentManager.getUsername())) {
                it.remove();
            }
        }
        return subteam;
    }

    ////// BOUND PROPERTIES

    public PscUser getResponsible() {
        return responsible;
    }

    public void setResponsible(PscUser responsible) {
        this.responsible = responsible;
    }

    public List<StudySubjectAssignment> getTargetAssignments() {
        return targetAssignments;
    }

    public void setTargetAssignments(List<StudySubjectAssignment> targetAssignments) {
        this.targetAssignments = targetAssignments;
    }

    public PscUser getNewResponsible() {
        return newResponsible;
    }

    public void setNewResponsible(PscUser newResponsible) {
        this.newResponsible = newResponsible;
    }

    public static class ReassignableAssignments {
        private StudySite studySite;
        private List<StudySubjectAssignment> assignments;
        private boolean stillManageable;
        private List<PscUser> eligibleUsers;

        private ReassignableAssignments(
            StudySite studySite, boolean stillManageable, List<PscUser> eligibleUsers
        ) {
            this.studySite = studySite;
            this.stillManageable = stillManageable;
            this.eligibleUsers = eligibleUsers;
            assignments = new ArrayList<StudySubjectAssignment>();
        }

        public StudySite getStudySite() {
            return studySite;
        }

        public boolean isStillManageable() {
            return stillManageable;
        }

        public List<StudySubjectAssignment> getAssignments() {
            return assignments;
        }

        public List<PscUser> getEligibleUsers() {
            return eligibleUsers;
        }
    }
}
