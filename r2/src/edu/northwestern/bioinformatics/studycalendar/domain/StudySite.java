
package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;



/**
 * @author Ram Chilukuri
 */
 @Entity
 @Table (name = "study_sites")
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_study_sites_id")
     }
 )

public class StudySite extends AbstractDomainObject {
    private Site site;
    private Study study;
    private String studyIdentifier;
    private List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
    public StudySite() {
    }

    public void setSite(Site site) {
        this.site = site;
    }
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id")
    public Site getSite() {
        return site;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "study_id")
    public Study getStudy() {
        return study;
    }
    
    
    public void setStudyIdentifier(String studyIdentifier) {
        this.studyIdentifier = studyIdentifier;
    }
    
    @Column(name="study_identifier")
    public String getStudyIdentifier() {
        return studyIdentifier;
    }
    
    // ************** Common Methods ***************** //
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!(obj instanceof StudySite)) return false;
         final StudySite studySite = (StudySite) obj;
         Study study = studySite.getStudy();
         Site site = studySite.getSite();
         if (!getStudy().equals(study)) return false;
         if (!getSite().equals(site)) return false;
         return true;
     }

     public int hashCode() {
         return getStudy().hashCode()+ getSite().hashCode();
     }

    public void setStudyParticipantAssignments(List<StudyParticipantAssignment> studyParticipantAssignments) {
        this.studyParticipantAssignments = studyParticipantAssignments;
    }

    @OneToMany (mappedBy = "studySite")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudyParticipantAssignment> getStudyParticipantAssignments() {
        return studyParticipantAssignments;
    }

}
