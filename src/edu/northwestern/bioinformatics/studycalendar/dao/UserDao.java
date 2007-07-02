package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;

import java.util.List;


public class UserDao extends StudyCalendarDao<User> {
    public Class<User> domainClass() {
        return User.class;
    }

    public List<User> getAll() {
        return getHibernateTemplate().find("from User");
    }

    public void save(User user) {
        getHibernateTemplate().saveOrUpdate(user);
    }

    public List getByName(String name) {
        List<User> results = getHibernateTemplate().find("from User where name = ?", name);
        return results;
    }

    public List getByCsmUserId(Long csmUserId) {
        List<User> results = getHibernateTemplate().find("from User where csm_user_id = ?", csmUserId);
        return results;
    }
}
