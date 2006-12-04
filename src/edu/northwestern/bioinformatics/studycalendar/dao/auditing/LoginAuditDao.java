package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.LoginAudit;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

public class LoginAuditDao extends StudyCalendarDao<LoginAudit> {
    public Class<LoginAudit> domainClass() {
        return LoginAudit.class;
    }

    public void save(LoginAudit loginAudit) {
        getHibernateTemplate().saveOrUpdate(loginAudit);
    }
}
