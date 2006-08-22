package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    private Date dateOfBirth;
    private String gender;
    private String socialSecurityNumber;
    private List<ParticipantIdentifier> participantIdentifiers = new ArrayList<ParticipantIdentifier>();
    private List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();

    // business methods
    
    // The participant identifier could be the Medical Record No based on the site 

    public void addParicipantIdentifier(ParticipantIdentifier participantIdentifier) {
    	participantIdentifiers.add(participantIdentifier);
    	participantIdentifier.setParticipant(this);
    }
    public void addStudyParticipantAssignments(StudyParticipantAssignment studyParticipantAssignment){
        getStudyParticipantAssignments().add(studyParticipantAssignment);
        studyParticipantAssignment.setParticipant(this);
    }
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Site)) return false;
        final Participant participant = (Participant) obj;
        if (!this.getFirstName().equals(participant.getFirstName())) return false;
        if (!this.getLastName().equals(participant.getLastName())) return false;
        if (!this.getDateOfBirth().equals(participant.getDateOfBirth())) return false;
        if (!this.getGender().equals(participant.getGender())) return false;
        if (!this.getSocialSecurityNumber().equals(participant.getSocialSecurityNumber())) return false;
        
        return true;
    }

    public int hashCode() {
        return this.getFirstName().hashCode();
    }

    // bean methods
    @Column(name = "first_name")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Column(name = "birth_date")
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
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
    public List<ParticipantIdentifier> getParticipantIdentifiers() {
        return participantIdentifiers;
    }

    public void setParticipantIdentifiers(List<ParticipantIdentifier> participantIdentifiers) {
        this.participantIdentifiers = participantIdentifiers;
    }
    @OneToMany (mappedBy = "participant")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudyParticipantAssignment> getStudyParticipantAssignments() {
        return studyParticipantAssignments;
    }
    
    public void setStudyParticipantAssignments(List<StudyParticipantAssignment> studyParticipantAssignments) {
        this.studyParticipantAssignments = studyParticipantAssignments;
    }
}
