package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;
import java.io.Serializable;

@Deprecated
public class UserDao extends StudyCalendarMutableDomainObjectDao<User> implements Serializable {
    @Override
    public Class<User> domainClass() {
        return User.class;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<User> getAll() {
        return getHibernateTemplate().find("from User order by name");
    }

    /**
     * Returns a bare {@link User} that corresponds to the given username. Most higher-level code
     * should not call this methid directly, but rather
     * {@link edu.northwestern.bioinformatics.studycalendar.service.UserService#getUserByName}.
     */
    @SuppressWarnings({"unchecked"})
    public User getByName(String name) {
        if (name == null) {
            return null;
        }
        return (User) CollectionUtils.firstElement(getHibernateTemplate().find("from User where name = ?", name));
    }

    @SuppressWarnings({"unchecked"})
    public List<StudySubjectAssignment> getAssignments(User user) {
        return (List<StudySubjectAssignment>) getHibernateTemplate().find(
                "from StudySubjectAssignment a where a.subjectCoordinator = ? ", user);
    }

    public List<User> getAllSubjectCoordinators() {
        return getByRole(Role.SUBJECT_COORDINATOR);
    }

    @SuppressWarnings({"unchecked"})
    public List<User> getByRole(Role role) {
        return getHibernateTemplate()
                .find("select u from User u join u.userRoles r where r.role = ? order by u.name", role);
    }

    @SuppressWarnings({"unchecked"})
    public List<User> getSiteCoordinators(Site from) {
        return getHibernateTemplate()
                .find("select u from User u join u.userRoles r where r.role = ? and ? in elements(r.sites)",
                        new Object[]{Role.SITE_COORDINATOR, from});
    }
}
