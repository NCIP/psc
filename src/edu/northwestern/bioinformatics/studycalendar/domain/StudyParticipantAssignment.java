package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


/**
 * @author Ram Chilukuri
 */
 
 @Entity
 @Table (name = "participant_assignments")
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_participant_assignments_id")
     }
 ) 
public class StudyParticipantAssignment extends AbstractDomainObject {
    private StudySite studySite;
    private Participant participant;
    private Date dateOfEnrollment;

    public void setStudySite(StudySite studySite) {
        this.studySite = studySite;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_site_id")
    public StudySite getStudySite() {
        return studySite;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    public Participant getParticipant() {
        return participant;
    }

    public void setDateOfEnrollment(Date enrollmentDate) {
        this.dateOfEnrollment = enrollmentDate;
    }
    
    @Column(name = "date_of_enrollment")
    public Date getDateOfEnrollment() {
        return dateOfEnrollment;
    }
}
