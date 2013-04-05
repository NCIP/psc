/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.service.auditing.AuditEventFactory;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.engine.CollectionEntry;
import org.hibernate.event.*;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jalpa Patel
 */

public class AuditEventListener implements PostDeleteEventListener, PostInsertEventListener, PostUpdateEventListener,
             PreCollectionUpdateEventListener, PreCollectionRemoveEventListener, PostCollectionRecreateEventListener {
    private AuditEventDao auditEventDao;
    private AuditEventFactory auditEventFactory;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public void onPostDelete(PostDeleteEvent event) {
        AuditEvent dataAuditEvent = auditEventFactory.createAuditEvent(event.getEntity(), Operation.DELETE);
        if (dataAuditEvent != null) {
            Type[] propertyTypes = event.getPersister().getPropertyTypes();
            String[] propertyNames =  event.getPersister().getPropertyNames();
            Object[] deletedState =  event.getDeletedState();
            for (int i = 0; i < deletedState.length; i++) {
                auditEventFactory.appendEventValues(dataAuditEvent, propertyTypes[i], propertyNames[i], deletedState[i], null);
            }
            auditEventDao.saveEvent(dataAuditEvent);
        }
    }

    public void onPostInsert(PostInsertEvent event) {
        AuditEvent dataAuditEvent = auditEventFactory.createAuditEvent(event.getEntity(), Operation.CREATE);
        if (dataAuditEvent != null) {
            Type[] propertyTypes = event.getPersister().getPropertyTypes();
            String[] propertyNames =  event.getPersister().getPropertyNames();
            Object[] state =  event.getState();
            for (int i = 0; i < state.length; i++) {
                auditEventFactory.appendEventValues(dataAuditEvent, propertyTypes[i], propertyNames[i],  null, state[i]);
            }
            auditEventDao.saveEvent(dataAuditEvent);
        }
    }

    public void onPostUpdate(PostUpdateEvent event) {
        if (event.getOldState() == null) {
            // this doesn't seem like it should be possible, but it does happen when running
            // the study service integration test.
            log.warn("Hibernate update of {} for audit event creation did not include old " +
                "state information.  Audit event for update operation not possible.", event.getEntity());
            return;
        } else {
            AuditEvent dataAuditEvent = auditEventFactory.createAuditEvent(event.getEntity(), Operation.UPDATE);
            if (dataAuditEvent != null) {
                Type[] propertyTypes = event.getPersister().getPropertyTypes();
                String[] propertyNames =  event.getPersister().getPropertyNames();
                Object[] state =  event.getState();
                Object[] oldState = event.getOldState();
                for (int i = 0; i < state.length; i++) {
                    auditEventFactory.appendEventValues(dataAuditEvent, propertyTypes[i], propertyNames[i], oldState[i], state[i]);
                }
                auditEventDao.saveEvent(dataAuditEvent);
            }
        }
    }

    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        Object obj = event.getAffectedOwnerOrNull();
        if (obj != null && obj instanceof AbstractMutableDomainObject) {
            CollectionEntry collectionEntry = event.getSession().getPersistenceContext().getCollectionEntry(event.getCollection());
            if (!collectionEntry.getLoadedPersister().isInverse() && !collectionEntry.getLoadedPersister().isOneToMany()) {
                String propertyName = collectionEntry.getRole().substring(event.getAffectedOwnerEntityName().length() + 1);
                AuditEvent dataAuditEvent = auditEventFactory.createAuditEvent(obj, Operation.UPDATE);
                auditEventFactory.appendCollectionEventValues(dataAuditEvent, propertyName,
                        collectionEntry.getSnapshot(), event.getCollection(), collectionEntry);
                auditEventDao.saveEvent(dataAuditEvent);
            } else {
                log.debug("No auditing for collection for instances of " + obj.getClass().getName());
            }
        }
    }

    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        Object obj = event.getAffectedOwnerOrNull();
        if (obj != null && obj instanceof AbstractMutableDomainObject) {
            CollectionEntry collectionEntry = event.getSession().getPersistenceContext().getCollectionEntry(event.getCollection());
            if (!collectionEntry.getLoadedPersister().isInverse() && !collectionEntry.getLoadedPersister().isOneToMany()) {
                String propertyName = collectionEntry.getRole().substring(event.getAffectedOwnerEntityName().length() + 1);
                AuditEvent dataAuditEvent = auditEventFactory.createAuditEvent(obj, Operation.UPDATE);
                auditEventFactory.appendCollectionEventValues(dataAuditEvent, propertyName, null,
                        event.getCollection(), collectionEntry);
                auditEventDao.saveEvent(dataAuditEvent);
            }
        }
    }

    public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
        Object obj = event.getAffectedOwnerOrNull();
        if (obj != null && obj instanceof AbstractMutableDomainObject) {
            CollectionEntry collectionEntry = event.getSession().getPersistenceContext().getCollectionEntry(event.getCollection());
            if (collectionEntry != null && !collectionEntry.getLoadedPersister().isInverse() && !collectionEntry.getLoadedPersister().isOneToMany()) {
                String propertyName = collectionEntry.getRole().substring(event.getAffectedOwnerEntityName().length() + 1);
                AuditEvent dataAuditEvent = auditEventFactory.createAuditEvent(obj, Operation.UPDATE);
                auditEventFactory.appendCollectionEventValues(dataAuditEvent, propertyName,
                        collectionEntry.getSnapshot(), null, collectionEntry);
                auditEventDao.saveEvent(dataAuditEvent);
            }
        }
    }

    public void setAuditEventDao(AuditEventDao auditEventDao) {
        this.auditEventDao = auditEventDao;
    }

    public void setAuditEventFactory(AuditEventFactory auditEventFactory) {
        this.auditEventFactory = auditEventFactory;
    }
}
