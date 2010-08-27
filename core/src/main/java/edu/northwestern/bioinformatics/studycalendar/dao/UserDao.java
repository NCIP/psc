package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.io.Serializable;

@Deprecated
public class UserDao extends StudyCalendarMutableDomainObjectDao<User> implements Serializable {
    @Override
    public Class<User> domainClass() {
        return User.class;
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
}
