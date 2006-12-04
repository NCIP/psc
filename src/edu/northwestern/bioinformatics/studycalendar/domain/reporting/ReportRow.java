package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.FieldResult;
import javax.persistence.EntityResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;


import java.util.Date;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */
@Entity
@NamedNativeQuery(
	name = "reportSqlQuery",
	query = "select " +
		"se.id as event_id," + 
		"act.name as event_name, " +
		"sem.name as state_name, " +
		"se.current_state_date as date," +
		"a.name as arm_name," +
		"e.name as epoch_name, " +
		"p.first_name as p_first_name, " +
		"p.last_name as p_last_name, " + 
		"st.name as study_name, " +
		"si.name as site_name "+
	"from "+
		"sites si inner join study_sites ss on si.id=ss.site_id " +
		"inner join studies st on st.id=ss.study_id " +
		"inner join participant_assignments pa on ss.id=pa.study_site_id " +
		"inner join participants p on pa.participant_id=p.id " +
		"inner join scheduled_calendars sc on pa.id=sc.assignment_id " +
		"inner join scheduled_arms sa on sc.id=sa.scheduled_calendar_id " +
		"inner join scheduled_events se on sa.id=se.scheduled_arm_id " +
		"inner join scheduled_event_modes sem on se.current_state_mode_id=sem.id " +
		"inner join planned_events pe on se.planned_event_id=pe.id " +
		"inner join activities act on pe.activity_id=act.id " +
		"inner join arms a on sa.arm_id=a.id " +
		"inner join epochs e on a.epoch_id=e.id " +
	"order by se.id",
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

@Transient
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

@Transient
public String getParticipantName() {
	return this.participantLastName + ", " + this.participantFirstName;
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


public String dateToDateStr(){
/*
DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
return this.dateStr = df.format(this.date);
*/
return this.dateStr = this.date.toString();
}
}