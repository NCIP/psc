package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;

import java.util.List;

public class UserRoleDao extends StudyCalendarMutableDomainObjectDao<UserRole> {
    @Override public Class<UserRole> domainClass() { return UserRole.class; }
}
