package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.OrderBy;
import javax.persistence.Transient;


import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_audit_event_values_id")
    }
)
public class AuditEventValue extends AbstractDomainObject {
	private AuditEvent auditEvent;
    private String attributeName;
    private String previousValue;
    private String newValue;
   
    // business methods
        
    // bean methods
    @Column(name = "attribute_name")
    public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	@Column(name = "previous_value")
	public String getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(String previousValue) {
		this.previousValue = previousValue;
	}

	@Column(name = "new_value")
	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	
    public void setAuditEvent(AuditEvent auditEvent) {
        this.auditEvent = auditEvent;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_event_id")
    public AuditEvent getAuditEvent() {
        return auditEvent;
    }
}
