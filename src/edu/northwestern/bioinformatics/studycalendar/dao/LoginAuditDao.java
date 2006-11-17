package edu.northwestern.bioinformatics.studycalendar.dao;

import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.LoginAudit;

public class LoginAuditDao extends StudyCalendarDao<LoginAudit> {
    public Class<LoginAudit> domainClass() {
        return LoginAudit.class;
    }

    public void save(LoginAudit loginAudit) {
        getHibernateTemplate().saveOrUpdate(loginAudit);
    }

    public List<LoginAudit> getAll() {
        return getHibernateTemplate().find("from LoginAudit");
    }
}
