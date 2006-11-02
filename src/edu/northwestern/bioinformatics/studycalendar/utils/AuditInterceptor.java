package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.nwu.bioinformatics.commons.ComparisonUtils;
import edu.nwu.bioinformatics.commons.DataAuditInfo;

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

/**
 * Spike to test whether auditing using a simple hibernate interceptor will work.
 *
 * @author Rhett Sutphin
 */
public class AuditInterceptor extends EmptyInterceptor {
    private static final Log log = LogFactory.getLog(AuditInterceptor.class);

    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        List<Integer> differences = findDifferences(currentState, previousState);

        for (int index : differences) {
            log("CHANGE: " + getEntityTypeName(entity) + '#' + id
                + ".  Changed " + propertyNames[index] + " from " + auditValue(previousState[index])
                + " to " + auditValue(currentState[index]) + '.');
        }

        return false;
    }


    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        log("CREATE: " + getEntityTypeName(entity) + '#' + id);
        log.info("        Initial values:");
        for (int i = 0; i < state.length; i++) {
            log.info("          - " + propertyNames[i] + " = " + auditValue(state[i]));
        }

        return false;
    }

    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        log("DELETE: " + getEntityTypeName(entity) + '#' + id);
    }

    private void log(String message) {
        DataAuditInfo info = DataAuditInfo.getLocal();
        log.info(info.getBy() + " connecting from " + info.getIp() + " performed " + message);
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
}
