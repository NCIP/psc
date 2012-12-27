/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;

import java.util.Date;

/**
 * @author Padmaja Vedula
 */

public class NewSubjectCommand {
	private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String personId;
    	
	public Subject createSubject() {
		Subject subject = new Subject();
		subject.setFirstName(getFirstName());
		subject.setLastName(getLastName());
		subject.setDateOfBirth(getDateOfBirth());
		subject.setGender(Gender.getByCode(getGender()));
		subject.setPersonId(getPersonId());
        return subject;
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
