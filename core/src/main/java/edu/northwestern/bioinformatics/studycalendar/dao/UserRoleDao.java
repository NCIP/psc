package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;

@Deprecated
public class UserRoleDao extends StudyCalendarMutableDomainObjectDao<UserRole> implements DeletableDomainObjectDao<UserRole> {
    @Override public Class<UserRole> domainClass() { return UserRole.class; }
    
    @SuppressWarnings({ "unchecked" })
    public List<UserRole> getUserRolesForSite(Site site) {
        // ordering is for testing consistency only
        return (List<UserRole>) getHibernateTemplate().find(
            "from UserRole ur where ? in elements(ur.sites) order by ur.id", site);
    }

    public void delete(UserRole userRole) {
        getHibernateTemplate().delete(userRole);
    }

    public void deleteAll(List<UserRole> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
