package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.auditing.DataAuditDao;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataReference;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.Operation;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AuditSession {
    private Map<DomainObject, DataAuditEvent> events = new IdentityHashMap<DomainObject, DataAuditEvent>();
    private DataAuditDao dataAuditDao;

    public AuditSession(DataAuditDao dataAuditDao) {
        this.dataAuditDao = dataAuditDao;
    }

    public void addEvent(DomainObject entity, DataAuditEvent event) {
        if (events.containsKey(entity)) {
            DataAuditEvent existingEvent = events.get(entity);
            throw new StudyCalendarSystemException("There is already an event (" + existingEvent.getOperation() + ") for " + entity + ".  Cannot register a new one (" + event.getOperation() + ')');
        }
        events.put(entity, event);
    }

    public void saveEvent(DomainObject entity) {
        if (!events.containsKey(entity)) return;

        if (entity.getId() == null) {
            throw new StudyCalendarSystemException("No ID for entity " + entity + "; cannot properly audit");
        }

        DataAuditEvent event = events.get(entity);
        if (event.getReference().getId() == null) {
            event.setReference(DataReference.create(entity));
        }
        events.remove(entity);
        dataAuditDao.save(event);
    }

    public boolean deleted(DomainObject entity) {
        DataAuditEvent event = events.get(entity);
        return event != null && event.getOperation() == Operation.DELETE;
    }

    public void close() {
        for (DomainObject entity : events.keySet()) {
            saveEvent(entity);
        }
        
        if (events.size() > 0) {
            throw new StudyCalendarSystemException("There are " + events.size() + " audit event(s) outstanding at the end of the hibernate session");
        }
    }
}
