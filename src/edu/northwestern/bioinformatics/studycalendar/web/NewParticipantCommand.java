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
    private String socialSecurityNumber;
	
	public Participant createParticipant() {
		Participant participant = new Participant();
		participant.setFirstName(getFirstName());
		participant.setLastName(getLastName());
		participant.setDateOfBirth(getDateOfBirth());
		participant.setGender(getGender());
		participant.setSocialSecurityNumber(getSocialSecurityNumber());        
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

	public String getSocialSecurityNumber() {
		return socialSecurityNumber;
	}

	public void setSocialSecurityNumber(String socialSecurityNumber) {
		this.socialSecurityNumber = socialSecurityNumber;
	}

}
