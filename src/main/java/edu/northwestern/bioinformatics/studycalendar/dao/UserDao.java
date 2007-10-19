package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;


public class UserDao extends StudyCalendarMutableDomainObjectDao<User> {
    @Override public Class<User> domainClass() { return User.class; }

    public List<User> getAll() {
        return getHibernateTemplate().find("from User order by name");
    }

    public User getByName(String name) {
        List<User> results = getHibernateTemplate().find("from User where name = ?", name);
        if (results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    public List getByCsmUserId(Long csmUserId) {
        List<User> results = getHibernateTemplate().find("from User where csm_user_id = ?", csmUserId);
        return results;
    }

    public List<StudyParticipantAssignment> getAssignments(User user) {
        List<StudyParticipantAssignment> results = getHibernateTemplate().find(
                "from StudyParticipantAssignment a where a.participantCoordinator = ? ", user);
        return results;
    }

    public List<User> getAllParticipantCoordinators() {
         List<User> results = getHibernateTemplate().find(
                 "select u  from User u join u.userRoles r " +
                           "where r.role = '" + Role.PARTICIPANT_COORDINATOR.csmGroup() + "'" +
                           "order by u.name");
        return results;
    }
}
