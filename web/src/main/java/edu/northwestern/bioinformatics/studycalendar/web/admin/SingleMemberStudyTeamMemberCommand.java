/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class SingleMemberStudyTeamMemberCommand extends BaseUserProvisioningCommand {
    // Map<Study Ident, ...>
    private Map<String, Map<PscRole, StudyTeamRoleMembership>> teamMemberships;

    private SingleMemberStudyTeamMemberCommand(
        PscUser user,
        ProvisioningSessionFactory provisioningSessionFactory,
        ApplicationSecurityManager applicationSecurityManager
    ) {
        super(user, provisioningSessionFactory, applicationSecurityManager);
        teamMemberships =
            new TreeMap<String, Map<PscRole, StudyTeamRoleMembership>>(ScopeComparator.IDENTITY);
    }

    public static SingleMemberStudyTeamMemberCommand create(
        PscUser existingUser,
        ProvisioningSessionFactory psFactory,
        ApplicationSecurityManager applicationSecurityManager,
        StudyDao studyDao, PscUser teamAdmin
    ) {
        if (teamAdmin.getMembership(PscRole.STUDY_TEAM_ADMINISTRATOR) == null) {
            throw new StudyCalendarSystemException(
                "%s is not authorized for this operation", teamAdmin);
        }

        SingleMemberStudyTeamMemberCommand command = new SingleMemberStudyTeamMemberCommand(
            existingUser, psFactory, applicationSecurityManager);

        command.setProvisionableRoles(PscRole.valuesProvisionableByStudyTeamAdministrator());
        command.setCanProvisionParticipateInAllStudies(true);
        List<Study> studies = studyDao.getVisibleStudiesForSiteParticipation(
            teamAdmin.getVisibleStudyParameters(PscRole.STUDY_TEAM_ADMINISTRATOR));
        command.setProvisionableParticipatingStudies(studies);

        for (PscRole role : PscRole.valuesProvisionableByStudyTeamAdministrator()) {
            command.registerMembership(JSON_ALL_SCOPE_IDENTIFIER, role, existingUser);
            for (Study study : studies) {
                command.registerMembership(study.getAssignedIdentifier(), role, existingUser);
            }
        }

        return command;
    }

    private void registerMembership(String studyIdent, PscRole role, PscUser user) {
        if (!teamMemberships.containsKey(studyIdent)) {
            teamMemberships.put(studyIdent, new LinkedHashMap<PscRole, StudyTeamRoleMembership>());
        }
        Map<PscRole, StudyTeamRoleMembership> studyMemberships = teamMemberships.get(studyIdent);
        studyMemberships.put(role, new StudyTeamRoleMembership(studyIdent, user, role));
    }

    public Map<String, Map<PscRole, StudyTeamRoleMembership>> getTeamMemberships() {
        return teamMemberships;
    }
}
