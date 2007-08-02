package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;


/**
 * @author Padmaja Vedula
 */

public class AssignParticipantCommand {
	 private Integer studyId;
	 private Integer studySiteId;
	 private Integer participantId;
	 private Date dateOfEnrollment;
	 private ParticipantDao participantDao;
	 private StudyDao studyDao;
	 private StudySiteDao studySiteDao;
	 
	 public Participant assignParticipant() {
		Participant participant = participantDao.getById(getParticipantId());
							
		StudyParticipantAssignment studyParticipantAssignment = new StudyParticipantAssignment();
		studyParticipantAssignment.setStudy(studyDao.getById(getStudyId()));
		studyParticipantAssignment.setStudySite(studySiteDao.getById(getStudySiteId()));
		studyParticipantAssignment.setParticipant(participant);
		studyParticipantAssignment.setDateOfEnrollment(this.getDateOfEnrollment());
			
		participant.addStudyParticipantAssignments(studyParticipantAssignment);
				
	    return participant;
	}
	    
	public void setParticipantDao(ParticipantDao participantDao) {
		this.participantDao = participantDao;
	}


	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}


	public void setStudySiteDao(StudySiteDao studySiteDao) {
		this.studySiteDao = studySiteDao;
	}


	public Date getDateOfEnrollment() {
		return dateOfEnrollment;
	}

	public void setDateOfEnrollment(Date dateOfEnrollment) {
		this.dateOfEnrollment = dateOfEnrollment;
	}

	public Integer getParticipantId() {
		return participantId;
	}


	public void setParticipantId(Integer participantId) {
		this.participantId = participantId;
	}

	public Integer getStudyId() {
		return studyId;
	}


	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}


	public Integer getStudySiteId() {
		return studySiteId;
	}


	public void setStudySiteId(Integer studySiteId) {
		this.studySiteId = studySiteId;
	}

}
