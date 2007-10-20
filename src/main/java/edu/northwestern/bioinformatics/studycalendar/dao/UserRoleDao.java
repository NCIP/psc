package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.User;

import java.util.List;

public class UserRoleDao extends StudyCalendarMutableDomainObjectDao<UserRole> {
    @Override public Class<UserRole> domainClass() { return UserRole.class; }

    public List<UserRole> getAllParticipantCoordinators() {
        List<UserRole> results = getHibernateTemplate().find(
                "select r from UserRole r join r.user u " +
                        "where r.role = '" + Role.PARTICIPANT_COORDINATOR.csmGroup() + "'" +
                        "order by u.name");
        return results;
    }

    public UserRole getByUserAndRole(User user, Role role) {
        List<UserRole> results = getHibernateTemplate().find(
                "select r from UserRole r join r.user u " +
                        "where r.user = ? " +
                        "and   r.role = '" + role.csmGroup() + "'" +
                        "order by u.name", user);
        return results.get(0);
    }
}
