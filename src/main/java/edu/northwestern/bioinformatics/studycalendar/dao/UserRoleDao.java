package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.List;

public class UserRoleDao extends StudyCalendarMutableDomainObjectDao<UserRole> {
    @Override public Class<UserRole> domainClass() { return UserRole.class; }

       public List<UserRole> getAllParticipantCoordinatorUserRoles() {
         List<UserRole> results = getHibernateTemplate().find(
                 "select r from UserRole r join r.user u " +
                           "where r.role = '" + Role.PARTICIPANT_COORDINATOR.csmGroup() + "'" +
                            "order by u.name");
        return results;
    }
}
