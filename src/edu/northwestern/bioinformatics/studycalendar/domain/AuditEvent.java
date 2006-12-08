package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.FetchType;
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
        @Parameter(name="sequence", value="seq_audit_events_id")
    }
)
public class AuditEvent extends AbstractDomainObject {
	private String ipAddress;  
	private String userName;  
	private Timestamp time;  
	private String className;  
	private String objectId;  
	private String operation;  
	private String url;  
	private List<AuditEventValue> auditEventValues = new ArrayList<AuditEventValue>();

   
    // business methods
        
    // bean methods
    @Column(name = "ip_address")
    public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	@Column(name = "object_id")
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	@Column(name = "class_name")
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	@Column(name = "user_name")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
    public void setAuditEventValues(List<AuditEventValue> auditEventValues) {
        this.auditEventValues = auditEventValues;
    }

    @OneToMany (mappedBy = "auditEvent",fetch = FetchType.EAGER)
    @OrderBy // order by ID for testing consistency
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<AuditEventValue> getAuditEventValues() {
        return auditEventValues;
    }
}
