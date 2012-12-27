/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
