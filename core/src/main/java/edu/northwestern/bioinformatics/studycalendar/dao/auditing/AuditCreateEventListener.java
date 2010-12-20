package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.type.Type;

/**
 * @author Jalpa Patel
 */
public class AuditCreateEventListener implements PostInsertEventListener {
    private AuditEventCreator auditEventCreator;

    public void onPostInsert(PostInsertEvent event) {
        AuditEvent dataAuditEvent = auditEventCreator.createAuditEvent(event.getEntity(), Operation.CREATE);
        if (dataAuditEvent != null) {
            Type[] propertyTypes = event.getPersister().getPropertyTypes();
            String[] propertyNames =  event.getPersister().getPropertyNames();
            Object[] state =  event.getState();
            for (int i = 0; i < state.length; i++) {
                dataAuditEvent.appendEventValues(propertyTypes[i], propertyNames[i], null, state[i]);
            }
            auditEventCreator.saveAuditEvent(dataAuditEvent);
        }
    }

    public void setAuditEventCreator(AuditEventCreator auditEventCreator) {
        this.auditEventCreator = auditEventCreator;
    }
}
