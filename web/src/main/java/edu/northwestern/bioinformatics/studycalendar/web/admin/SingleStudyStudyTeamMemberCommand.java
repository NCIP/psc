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
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SingleStudyStudyTeamMemberCommand
    extends BaseUserProvisioningCommand
    implements PscAuthorizedCommand
{
    private Map<PscUser, Map<PscRole, StudyTeamRoleMembership>> teamMemberships;

    protected SingleStudyStudyTeamMemberCommand(
        List<PscUser> users,
        ProvisioningSessionFactory provisioningSessionFactory,
        ApplicationSecurityManager applicationSecurityManager
    ) {
        super(users, provisioningSessionFactory, applicationSecurityManager);
        teamMemberships = new LinkedHashMap<PscUser, Map<PscRole, StudyTeamRoleMembership>>();
    }

    public static SingleStudyStudyTeamMemberCommand create(
        Study study, List<PscUser> users, ProvisioningSessionFactory psFactory,
        ApplicationSecurityManager applicationSecurityManager, StudyDao studyDao,
        PscUser teamAdmin
    ) {
        if (teamAdmin.getMembership(PscRole.STUDY_TEAM_ADMINISTRATOR) == null) {
            // this should never happen -- unauthorized users should be blocked in the controller
            throw new StudyCalendarSystemException(
                "%s is not authorized for this operation", teamAdmin.getUsername());
        }

        SingleStudyStudyTeamMemberCommand command =
            new SingleStudyStudyTeamMemberCommand(users, psFactory, applicationSecurityManager);

        command.setProvisionableRoles(PscRole.valuesProvisionableByStudyTeamAdministrator());
        command.setCanProvisionParticipateInAllStudies(false);
        List<Study> studies = studyDao.getVisibleStudiesForSiteParticipation(
            teamAdmin.getVisibleStudyParameters(PscRole.STUDY_TEAM_ADMINISTRATOR));
        if (studies.contains(study)) {
            command.setProvisionableParticipatingStudies(Collections.singletonList(study));
        } else {
            // this should never happen -- unauthorized users should be blocked in the controller
            throw new StudyCalendarSystemException(
                "%s is not authorized for this operation", teamAdmin.getUsername());
        }

        for (PscRole role : PscRole.valuesProvisionableByStudyTeamAdministrator()) {
            for (PscUser user : users) {
                command.registerMembership(study.getAssignedIdentifier(), role, user);
            }
        }

        return command;
    }

    private void registerMembership(String studyIdent, PscRole role, PscUser user) {
        if (!teamMemberships.containsKey(user)) {
            teamMemberships.put(user, new LinkedHashMap<PscRole, StudyTeamRoleMembership>());
        }
        Map<PscRole, StudyTeamRoleMembership> studyMemberships = teamMemberships.get(user);
        studyMemberships.put(role, new StudyTeamRoleMembership(studyIdent, user, role));
    }

    ////// AUTHORIZATION

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        List<ResourceAuthorization> authorizations = new ArrayList<ResourceAuthorization>();
        for (Site site : getStudy().getSites()) {
            authorizations.add(ResourceAuthorization.create(PscRole.STUDY_TEAM_ADMINISTRATOR, site));
        }
        return authorizations;
    }

    ////// CONFIGURATION

    public Study getStudy() {
        return getProvisionableParticipatingStudies().isEmpty() ?
            null : 
            getProvisionableParticipatingStudies().get(0);
    }

    public Map<PscUser, Map<PscRole, StudyTeamRoleMembership>> getTeamMemberships() {
        return teamMemberships;
    }
}
