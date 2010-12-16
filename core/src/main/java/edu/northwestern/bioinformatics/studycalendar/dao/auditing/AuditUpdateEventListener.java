package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jalpa Patel
 */

public class AuditUpdateEventListener implements PostUpdateEventListener {
    private AuditEventHelper auditEventHelper;
    private final Logger log = LoggerFactory.getLogger(getClass());
    public void onPostUpdate(PostUpdateEvent event) {
        if (event.getOldState() == null) {
            // this doesn't seem like it should be possible, but it does happen when running
            // the study service integration test.
            log.warn("Hibernate update of {} for audit event creation did not include old " +
                "state information.  Audit event for update operation not possible.", event.getEntity());
            return;
        } else {
            AuditEvent dataAuditEvent = auditEventHelper.createAuditEvent(event.getEntity(), Operation.UPDATE);
            if (dataAuditEvent != null) {
                Type[] propertyTypes = event.getPersister().getPropertyTypes();
                String[] propertyNames =  event.getPersister().getPropertyNames();
                Object[] state =  event.getState();
                Object[] oldState = event.getOldState();
                for (int i = 0; i < state.length; i++) {
                    dataAuditEvent.appendEventValues(propertyTypes[i], propertyNames[i], oldState[i], state[i]);
                }
                auditEventHelper.saveAuditEvent(dataAuditEvent);
            }
        }
    }

    public void setAuditEventHelper(AuditEventHelper auditEventHelper) {
        this.auditEventHelper = auditEventHelper;
    }
}
