package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.FieldResult;
import javax.persistence.EntityResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;


import java.util.ArrayList;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Yufang Wang
 */
@Entity
@NamedNativeQuery(
	name = "reportSqlQuery",
	query = "select " +
				"se.id as event_id, act.name as event_name, ses.name as state_name, se.actual_date as date, " +
				"a.name as arm_name, e.name as epoch_name, p.first_name as p_first_name, p.last_name as p_last_name, " +
				"st.name as study_name, si.name as site_name " + 
			" from " + 
				"scheduled_arms sa, planned_events pe, scheduled_calendars sc, " + 
				"participant_assignments pa, study_sites ss, activities act, " +
				"scheduled_event_states ses, scheduled_events se, arms a, epochs e, " +  
				"participants p, studies st, sites si " +
			" where " + 
				"si.id in (150,155) and st.id in (94,96,97,98) and p.id in (2,4) " + 
				"and ss.site_id=si.id and ss.study_id=st.id " +
				"and pa.participant_id=p.id and pa.study_site_id=ss.id " +
				"and sc.assignment_id=pa.id " +
				"and sa.scheduled_calendar_id=sc.id " +
				"and se.scheduled_arm_id=sa.id " +
				"and se.actual_date between '01-01-2001' and '12-31-2006' " + 
				"and ses.id=se.scheduled_event_state_id " +
				"and pe.id=se.planned_event_id " +
				"and act.id=pe.activity_id " +
				"and a.id=sa.arm_id and e.id=a.epoch_id;",
	resultSetMapping = "reportSqlQueryMapping"
)
@SqlResultSetMapping(
	name = "reportSqlQueryMapping",
	entities = @EntityResult(entityClass = edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow.class,
				             fields = { @FieldResult( name = "eventId", column = "event_id"),
										@FieldResult( name = "eventName", column = "event_name"),
										@FieldResult( name = "currentState", column = "state_name"),
										@FieldResult( name = "date", column = "date"),
										@FieldResult( name = "armName", column = "arm_name"),
										@FieldResult( name = "epochName", column = "epoch_name"),
										@FieldResult( name = "participantFirstName", column = "p_first_name"),
										@FieldResult( name = "participantLastName", column = "p_last_name"),
										@FieldResult( name = "studyName", column = "study_name"),
										@FieldResult( name = "siteName", column = "site_name") 
							})
)
public class ReportRow extends HibernateDaoSupport {
	private Date date;
	private String dateStr;
	private String studyName;
	private String siteName;
	private String participantFirstName;
	private String participantLastName;
	private String participantName;
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
	
	public void setDateStr(String date){
		this.dateStr = date;
	}
		
	public String getDateStr(){
		return this.dateStr;
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

	public void setParticipantFirstName(String pfname) {
		this.participantFirstName = pfname;
	}
		
	public String getParticipantFirstName() {
		return this.participantFirstName;
	}

	public void setParticipantLastName(String plname) {
		this.participantLastName = plname;
	}
		
	public String getParticipantLastName() {
		return this.participantLastName;
	}
	
	public void setParticipantName(String pname) {
		this.participantName = pname;
	}
		
	public String getParticipantName() {
		return this.participantName;
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
	
	public String generateParticipantName(String pfname, String plname) {
		return this.participantName = plname + ", " + pfname;
	}
	
	public String dateToDateStr(){
		/*
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		return this.dateStr = df.format(this.date);
		*/
		return this.dateStr = this.date.toString();
	}
}