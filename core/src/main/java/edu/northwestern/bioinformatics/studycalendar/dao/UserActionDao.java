package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;

public class UserActionDao extends StudyCalendarMutableDomainObjectDao<UserAction>{
    @Override
    public Class<UserAction> domainClass() {
        return UserAction.class;
    }
}
