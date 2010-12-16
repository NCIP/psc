package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.type.Type;

/**
 * @author Jalpa Patel
 */
public class AuditDeleteEventListener implements PostDeleteEventListener {
    private AuditEventHelper auditEventHelper;
    public void onPostDelete(PostDeleteEvent event) {
        AuditEvent dataAuditEvent = auditEventHelper.createAuditEvent(event.getEntity(), Operation.DELETE);
        if (dataAuditEvent != null) {
            Type[] propertyTypes = event.getPersister().getPropertyTypes();
            String[] propertyNames =  event.getPersister().getPropertyNames();
            Object[] deletedState =  event.getDeletedState();
            for (int i = 0; i < deletedState.length; i++) {
                dataAuditEvent.appendEventValues(propertyTypes[i], propertyNames[i], deletedState[i], null);
            }
            auditEventHelper.saveAuditEvent(dataAuditEvent);
        }
    }

    public void setAuditEventHelper(AuditEventHelper auditEventHelper) {
        this.auditEventHelper = auditEventHelper;
    }
}
