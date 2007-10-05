package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;


public class UserDao extends StudyCalendarMutableDomainObjectDao<User> {
    @Override public Class<User> domainClass() { return User.class; }

    public List<User> getAll() {
        return getHibernateTemplate().find("from User order by name");
    }

    public User getByName(String name) {
        List<User> results = getHibernateTemplate().find("from User where name = ?", name);
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
}
