package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.Operation;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEventValue;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;
import edu.northwestern.bioinformatics.studycalendar.dao.auditing.DataAuditDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 *
 *
 * @author Rhett Sutphin
 */
public class AuditInterceptor extends EmptyInterceptor {
    private static final Log log = LogFactory.getLog(AuditInterceptor.class);
    private static final ThreadLocal<AuditSession> sessions = new ThreadLocal<AuditSession>();

    private DataAuditDao dataAuditDao;

    public boolean onFlushDirty(
        Object entity, Serializable id, Object[] currentState, Object[] previousState,
        String[] propertyNames, Type[] types
    ) {
        if (!auditable(entity)) return false;
        DomainObject dEntity = (DomainObject) entity;
        if (getAuditSession().deleted(dEntity)) return false;
        
        List<Integer> differences = findDifferences(currentState, previousState);
        if (differences.size() == 0) return false;

        DataAuditEvent event = registerEvent(dEntity, Operation.UPDATE);

        for (int index : differences) {
            event.addValue(new DataAuditEventValue(
                propertyNames[index], auditValue(previousState[index]), auditValue(currentState[index])
            ));
        }

        return false;
    }

    public boolean onSave(
        Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types
    ) {
        if (!auditable(entity)) return false;

        registerEvent((DomainObject) entity, Operation.CREATE);

        return false;
    }

    public void onDelete(
        Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types
    ) {
        if (!auditable(entity)) return;

        registerEvent((DomainObject) entity, Operation.DELETE);
    }

    public void postFlush(Iterator entities) {
        while (entities.hasNext()) {
            Object entity = entities.next();
            if (!auditable(entity)) continue;
            getAuditSession().saveEvent((DomainObject) entity);
        }
        closeAuditSession();
    }

    private String getEntityTypeName(Object entity) {
        return entity.getClass().getSimpleName();
    }

    // package-level for testing
    List<Integer> findDifferences(Object[] currentState, Object[] previousState) {
        List<Integer> differences = new ArrayList<Integer>();
        for (int i = 0; i < currentState.length; i++) {
            if (!ComparisonUtils.nullSafeEquals(currentState[i], previousState[i])) {
                differences.add(i);
            }
        }
        return differences;
    }

    String auditValue(Object propertyValue) {
        if (propertyValue == null) {
            return null;
        } else if (propertyValue instanceof DomainObject) {
            Integer id = ((DomainObject) propertyValue).getId();
            return id == null ? "transient " + getEntityTypeName(propertyValue) : id.toString();
        } else if (propertyValue instanceof Collection) {
            StringBuilder audit = new StringBuilder();
            for (Iterator<?> it = ((Iterable<?>) propertyValue).iterator(); it.hasNext();) {
                Object element = it.next();
                audit.append(auditValue(element));
                if (it.hasNext()) audit.append(", ");
            }
            return audit.toString();
        } else {
            return propertyValue.toString();
        }
    }

    private DataAuditEvent registerEvent(DomainObject entity, Operation operation) {
        DataAuditEvent event = new DataAuditEvent(entity, operation);
        DataAuditInfo info = (DataAuditInfo) DataAuditInfo.getLocal();
        if (info == null) {
            throw new StudyCalendarSystemException("Cannot audit; no local audit info available");
        }
        event.setInfo(DataAuditInfo.copy(info));
        getAuditSession().addEvent(entity, event);
        return event;
    }

    private AuditSession getAuditSession() {
        if (sessions.get() == null) {
            sessions.set(new AuditSession(dataAuditDao));
        }
        return sessions.get();
    }

    private void closeAuditSession() {
        getAuditSession().close();
        sessions.set(null);
    }

    private boolean auditable(Object entity) {
        boolean auditable = true;
        auditable &= entity instanceof DomainObject;
        auditable &= entity.getClass().getName().indexOf("auditing") < 0;
        if (auditable) {
            return true;
        } else {
            log.debug("No auditing for instances of " + entity.getClass().getName());
            return false;
        }
    }

    ////// CONFIGURATION

    public void setDataAuditDao(DataAuditDao dataAuditDao) {
        this.dataAuditDao = dataAuditDao;
    }
}
