package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "audit_event_values")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_audit_event_values_id")
    }
)
public class DataAuditEventValue extends AbstractMutableDomainObject {
    private DataAuditEvent auditEvent;
    private String attributeName;
    private String previousValue;
    private String currentValue;

    /* for hibernate */
    protected DataAuditEventValue() { }

    public DataAuditEventValue(String attributeName, String previousValue, String currentValue) {
        this.attributeName = attributeName;
        this.previousValue = previousValue;
        this.currentValue = currentValue;
    }

    ////// BEAN PROPERTIES

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String newValue) {
        this.currentValue = newValue;
    }

    public void setAuditEvent(DataAuditEvent auditEvent) {
        this.auditEvent = auditEvent;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_event_id")
    public DataAuditEvent getAuditEvent() {
        return auditEvent;
    }
}
