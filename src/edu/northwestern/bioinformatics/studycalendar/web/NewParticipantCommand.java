package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Date;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;

/**
 * @author Padmaja Vedula
 */

public class NewParticipantCommand {
	private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String personId;
    	
	public Participant createParticipant() {
		Participant participant = new Participant();
		participant.setFirstName(getFirstName());
		participant.setLastName(getLastName());
		participant.setDateOfBirth(getDateOfBirth());
		participant.setGender(getGender());
		participant.setPersonId(getPersonId());        
        return participant;
    }

	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

}
