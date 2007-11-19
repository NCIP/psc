package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.FieldResult;
import javax.persistence.EntityResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;


import java.text.DateFormat;
import java.util.Date;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */
@Entity
@SqlResultSetMapping(
	name = "reportSqlQueryMapping",
	entities = @EntityResult(entityClass = edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow.class,
				             fields = { @FieldResult( name = "eventId", column = "event_id"),
										@FieldResult( name = "eventName", column = "event_name"),
										@FieldResult( name = "currentState", column = "state_name"),
										@FieldResult( name = "date", column = "date"),
										@FieldResult( name = "idealDate", column = "ideal_date"),
										@FieldResult( name = "armName", column = "arm_name"),
										@FieldResult( name = "epochName", column = "epoch_name"),
										@FieldResult( name = "subjectFirstName", column = "p_first_name"),
										@FieldResult( name = "subjectLastName", column = "p_last_name"),
										@FieldResult( name = "studyName", column = "study_name"),
										@FieldResult( name = "siteName", column = "site_name")
				            }

	)
)

public class ReportRow extends HibernateDaoSupport {
	private Date date;
	private Date idealDate;
	private String studyName;
	private String siteName;
	private String subjectFirstName;
	private String subjectLastName;
	private String eventName;
	private String epochName;
	private String armName;
	private String currentState;
	private Integer eventId;
	
	
	public void setEventId(Integer evid){
	this.eventId = evid;
	}
	
	@Id
	public Integer getEventId(){
	return this.eventId;
	}
	
	public void setDate(Date date){
	this.date = date;
	}
	
	public Date getDate(){
	return this.date;
	}
	
	@Transient
	public String getDateStr(){
		String dateStr = new String();
		if(this.date != null) {
			dateStr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(this.date);
		} else {
			dateStr = "N/A";
		}
	return dateStr;
	}
	
	public Date getIdealDate() {
		return idealDate;
	}

	public void setIdealDate(Date idealDate) {
		this.idealDate = idealDate;
	}

	@Transient
	public String getIdealDateStr(){
		String dateStr = new String();
		if(this.idealDate != null) {
			dateStr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(this.idealDate);
		} else {
			dateStr = "N/A";
		}
	return dateStr;
	}

	public void setStudyName(String studyname) {
	this.studyName = studyname;
	}
	
	public String getStudyName() {
	return this.studyName;
	}
	
	public void setSiteName(String sitename) {
	this.siteName = sitename;
	}
	
	public String getSiteName() {
	return this.siteName;
	}
	
	public void setSubjectFirstName(String pfname) {
	this.subjectFirstName = pfname;
	}
	
	public String getSubjectFirstName() {
	return this.subjectFirstName;
	}
	
	public void setSubjectLastName(String plname) {
	this.subjectLastName = plname;
	}
	
	public String getSubjectLastName() {
	return this.subjectLastName;
	}
	
	@Transient
	public String getSubjectName() {
		return this.subjectFirstName + " " + this.subjectLastName;
	}
	
	public void setEventName(String eventname) {
	this.eventName = eventname;
	}
	
	public String getEventName() {
	return this.eventName;
	}
	
	public void setCurrentState(String state){
	this.currentState = state;
	}
	
	public String getCurrentState(){
	return this.currentState;
	}
	
	public void setEpochName(String epochname) {
	this.epochName = epochname;
	}
	
	public String getEpochName() {
	return this.epochName;
	}
	
	public void setArmName(String armname) {
	this.armName = armname;
	}
	
	public String getArmName() {
	return this.armName;
	}

}
/*public String dateToDateStr(){

DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
return this.dateStr = df.format(this.date);

return this.dateStr = this.date.toString();
}
}*/