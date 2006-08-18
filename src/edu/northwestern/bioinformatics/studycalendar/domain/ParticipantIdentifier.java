package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.UniqueConstraint;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table (name = "participant_identifiers", 
		uniqueConstraints = {@UniqueConstraint(columnNames={"medical_record_number", "site_id"})}
)
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_participant_identifiers_id")
    }
)
public class ParticipantIdentifier extends AbstractDomainObject {
    
	private String medicalRecordNumber;
    private Participant participant;
    private String type;
    private String description;
    private Site site;

    ////// BEAN PROPERTIES
    @Column(name = "medical_record_number", nullable = false)
    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public void setMedicalRecordNumber(String medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @Column(name = "identifier_type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
    	return description;
    }
    
    public void setDescription(String description){
    	this.description = description;
    }
}
