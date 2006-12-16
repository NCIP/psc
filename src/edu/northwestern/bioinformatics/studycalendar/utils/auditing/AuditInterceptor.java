package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.EmptyInterceptor;
import org.hibernate.SessionFactory;
import org.hibernate.EntityMode;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.hibernate.type.ComponentType;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.VersionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Collections;
import java.lang.reflect.InvocationTargetException;

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
    private static final EntityMode ENTITY_MODE = EntityMode.POJO;

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
            appendEventValues(event, types[index], propertyNames[index],
                previousState[index], currentState[index]);
        }

        return false;
    }

    public boolean onSave(
        Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types
    ) {
        if (!auditable(entity)) return false;

        DataAuditEvent event = registerEvent((DomainObject) entity, Operation.CREATE);

        for (int i = 0; i < state.length; i++) {
            appendEventValues(event, types[i], propertyNames[i], null, state[i]);
        }

        return false;
    }

    public void onDelete(
        Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types
    ) {
        if (!auditable(entity)) return;

        DataAuditEvent delete = registerEvent((DomainObject) entity, Operation.DELETE);
        for (int i = 0; i < state.length; i++) {
            appendEventValues(delete, types[i], propertyNames[i], state[i], null);
        }
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
        if (currentState == null || previousState == null) {
            int len = currentState == null ? previousState.length : currentState.length;
            while (differences.size() < len) {
                differences.add(differences.size());
            }
        } else {
            for (int i = 0; i < currentState.length; i++) {
                if (!ComparisonUtils.nullSafeEquals(currentState[i], previousState[i])) {
                    differences.add(i);
                }
            }
        }
        return differences;
    }

    private void appendEventValues(
        DataAuditEvent parent,
        Type propertyType, String propertyName, Object previousState, Object currentState
    ) {
        List<DataAuditEventValue> values = Collections.emptyList();
        if (propertyType instanceof VersionType) {
            // none
        } else if (propertyType.isComponentType()) {
            values = decomposeComponent((AbstractComponentType) propertyType,
                    propertyName, previousState, currentState);
        } else {
            String prevValue = scalarAuditableValue(previousState);
            String curValue = scalarAuditableValue(currentState);
            values = Arrays.asList(new DataAuditEventValue(
                propertyName, prevValue, curValue
            ));
        }
        for (DataAuditEventValue value : values) parent.addValue(value);
    }

    // TODO: this only handles one level of components
    private List<DataAuditEventValue> decomposeComponent(AbstractComponentType propertyType, String propertyName, Object previousState, Object currentState) {
        Object[] componentPrevState = previousState == null ? null : propertyType.getPropertyValues(previousState, ENTITY_MODE);
        Object[] componentCurState = currentState == null ? null : propertyType.getPropertyValues(currentState, ENTITY_MODE);
        List<Integer> differences = findDifferences(componentCurState, componentPrevState);
        List<DataAuditEventValue> values = new ArrayList<DataAuditEventValue>(differences.size());
        for (Integer index : differences) {
            String compPropertyName = propertyName + '.' + propertyType.getPropertyNames()[index];
            values.add(new DataAuditEventValue(
                compPropertyName,
                previousState == null ? null : scalarAuditableValue(componentPrevState[index]),
                currentState == null  ? null : scalarAuditableValue(componentCurState[index])
            ));
        }
        return values;
    }

    String scalarAuditableValue(Object propertyValue) {
        if (propertyValue == null) {
            return null;
        } else if (propertyValue instanceof DomainObject) {
            Integer id = ((DomainObject) propertyValue).getId();
            return id == null ? "transient " + getEntityTypeName(propertyValue) : id.toString();
        } else if (propertyValue instanceof Collection) {
            StringBuilder audit = new StringBuilder();
            audit.append('[');
            for (Iterator<?> it = ((Iterable<?>) propertyValue).iterator(); it.hasNext();) {
                Object element = it.next();
                audit.append(scalarAuditableValue(element));
                if (it.hasNext()) audit.append(", ");
            }
            audit.append(']');
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

    @Required
    public void setDataAuditDao(DataAuditDao dataAuditDao) {
        this.dataAuditDao = dataAuditDao;
    }
}
