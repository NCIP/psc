package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.LoginAudit;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;

public class LoginAuditDao extends StudyCalendarMutableDomainObjectDao<LoginAudit> {
    @Override
    public Class<LoginAudit> domainClass() {
        return LoginAudit.class;
    }
}
