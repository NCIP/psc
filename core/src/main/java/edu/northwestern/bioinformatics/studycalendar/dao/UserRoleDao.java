package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;

public class UserRoleDao extends StudyCalendarMutableDomainObjectDao<UserRole> {
    @Override public Class<UserRole> domainClass() { return UserRole.class; }
    
    @SuppressWarnings({ "unchecked" })
    public List<UserRole> getUserRolesForSite(Site site) {
        return (List<UserRole>) getHibernateTemplate().find(
                "from UserRole ur where ? in elements(ur.sites)", site);
    }
}
