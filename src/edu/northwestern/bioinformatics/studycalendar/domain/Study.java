package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "studies")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_studies_id")
    }
)
public class Study extends AbstractDomainObject {
    private String name;
    private List<Arm> arms = new ArrayList<Arm>();
    private boolean completed;
    private List<StudySite> studySites = new ArrayList<StudySite>();
    private List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();

    
    public Study() {
    	setCompleted(false);
    }

    ////// LOGIC
    

    public void addArm(Arm arm) {
        arms.add(arm);
        arm.setStudy(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Arm arm : arms) {
            len = Math.max(len, arm.getLengthInDays());
        }
        return len;
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isCompleted() {
    	return completed;
    }

    public void setCompleted(boolean completed) {
    	this.completed = completed;
    }

    @OneToMany (mappedBy = "study")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }
    
    public void setStudySites(List<StudySite> studySites) {
        this.studySites = studySites;
    }

    @OneToMany (mappedBy = "study",fetch = FetchType.EAGER)
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudySite> getStudySites() {
        return studySites;
    }
    
    public void addStudySite(StudySite studySite){
    
        getStudySites().add(studySite);
        
        studySite.setStudy(this);
        
    }

    public void setStudyParticipantAssignments(List<StudyParticipantAssignment> studyParticipantAssignments) {
        this.studyParticipantAssignments = studyParticipantAssignments;
    }

    @OneToMany (mappedBy = "study")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudyParticipantAssignment> getStudyParticipantAssignments() {
        return studyParticipantAssignments;
    }

}
