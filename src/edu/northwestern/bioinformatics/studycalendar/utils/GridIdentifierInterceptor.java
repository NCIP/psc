package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.hibernate.type.Type;
import org.hibernate.EntityMode;
import org.hibernate.Transaction;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Arrays;

import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;

/**
 * Wrapper interceptor to add grid identifiers to objects which support them.
 * A cleaner implementation would be to have a separate compound interceptor implementation
 * that delegated to this class and to {@link edu.northwestern.bioinformatics.studycalendar.utils.auditing.AuditInterceptor}; unfortunately, Hibernate's
 * {@link org.hibernate.Interceptor} interface does not permit this.  (It passes single-use
 * objects as parameters -- specifically, {@link java.util.Iterator}s.  This is presumably why
 * hibernate itself only allows one interceptor at a time.)
 *
 * @author Rhett Sutphin
 */
public class GridIdentifierInterceptor implements Interceptor {
    private GridIdentifierCreator gridIdentifierCreator;
    private Interceptor delegate;

    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean localMod = false;
        if (entity instanceof GridIdentifiable) {
            int bigIdIdx = findBigId(propertyNames);
            if (bigIdIdx < 0) {
                throw new StudyCalendarError(
                    "Object implements GridIdentifiable but doesn't have gridId property; class: " + entity.getClass().getName() + "; properties: " + Arrays.asList(propertyNames));
            }
            if (state[bigIdIdx] == null) {
                state[bigIdIdx] = gridIdentifierCreator.getGridIdentifier();
                localMod = true;
            }
        }
        boolean delegateMod = delegate.onSave(entity, id, state, propertyNames, types);
        return localMod || delegateMod;
    }

    private int findBigId(String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if ("gridId".equals(propertyNames[i])) return i;
        }
        return -1; // defer throwing exception so we can report class
    }

    ////// CONFIGURATION

    public void setGridIdentifierCreator(GridIdentifierCreator gridIdentifierCreator) {
        this.gridIdentifierCreator = gridIdentifierCreator;
    }

    public void setDelegate(Interceptor delegate) {
        this.delegate = delegate;
    }

    ////// REMAINING METHODS ARE PURE DELEGATION

    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return delegate.onLoad(entity, id, state, propertyNames, types);
    }

    public void postFlush(Iterator entities) {
        delegate.postFlush(entities);
    }

    public void preFlush(Iterator entities) {
        delegate.preFlush(entities);
    }

    public Boolean isTransient(Object entity) {
        return delegate.isTransient(entity);
    }

    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        return delegate.instantiate(entityName, entityMode, id);
    }

    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return delegate.findDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    public String getEntityName(Object object) {
        return delegate.getEntityName(object);
    }

    public Object getEntity(String entityName, Serializable id) {
        return delegate.getEntity(entityName, id);
    }

    public void afterTransactionBegin(Transaction tx) {
        delegate.afterTransactionBegin(tx);
    }

    public void afterTransactionCompletion(Transaction tx) {
        delegate.afterTransactionCompletion(tx);
    }

    public void beforeTransactionCompletion(Transaction tx) {
        delegate.beforeTransactionCompletion(tx);
    }

    public String onPrepareStatement(String sql) {
        return delegate.onPrepareStatement(sql);
    }

    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
        delegate.onCollectionRemove(collection, key);
    }

    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        delegate.onCollectionRecreate(collection, key);
    }

    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        delegate.onCollectionUpdate(collection, key);
    }

    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return delegate.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        delegate.onDelete(entity, id, state, propertyNames, types);
    }
}
