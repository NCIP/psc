package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.OrderBy;
import javax.persistence.Transient;


import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_login_audits_id")
    }
)
public class LoginAudit extends AbstractDomainObject {
    private String ipAddress;
    private String userName;
    private Timestamp time;
    private String loginStatus;
   
    // business methods
        
    // bean methods
    @Column(name = "ip_address")
    public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "login_status")
	public String getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
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
}
