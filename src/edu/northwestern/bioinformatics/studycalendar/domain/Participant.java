package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;


import java.util.HashSet;
import java.util.Set;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table (name = "participants")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_participants_id")
    }
)
public class Participant extends AbstractDomainObject {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String socialSecurityNumber;
    private Set<ParticipantIdentifier> participantIdentifiers = new HashSet<ParticipantIdentifier>();

    // business methods
    
    // The participant identifier could be the Medical Record No based on the site 

    public void addParicipantIdentifier(ParticipantIdentifier participantIdentifier) {
    	participantIdentifiers.add(participantIdentifier);
    	participantIdentifier.setParticipant(this);
    }

    // bean methods

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    
    @Column(name = "social_security_number", unique = true, nullable = false)
    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }
    

    @OneToMany (mappedBy = "participant")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<ParticipantIdentifier> getParticipantIdentifiers() {
        return participantIdentifiers;
    }

    public void setParticipantIdentifiers(Set<ParticipantIdentifier> participantIdentifiers) {
        this.participantIdentifiers = participantIdentifiers;
    }
}
