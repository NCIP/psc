package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataReference;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DataAuditDao extends StudyCalendarDao<DataAuditEvent> {
    public Class<DataAuditEvent> domainClass() {
        return DataAuditEvent.class;
    }

    public void save(DataAuditEvent auditEvent) {
        getHibernateTemplate().saveOrUpdate(auditEvent);
    }

    public List<DataAuditEvent> getAuditTrail(DomainObject object) {
        return getAuditTrail(DataReference.create(object));
    }

    public List<DataAuditEvent> getAuditTrail(DataReference ref) {
        return getHibernateTemplate()
            .find("from DataAuditEvent e where e.reference.className = ? and e.reference.id = ? order by e.info.time",
                new Object[] { ref.getClassName(), ref.getId() });
    }
}
