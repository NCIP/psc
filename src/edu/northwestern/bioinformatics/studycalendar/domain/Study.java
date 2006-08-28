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
import javax.persistence.OneToOne;
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
    private PlannedSchedule plannedSchedule;
    private List<StudySite> studySites = new ArrayList<StudySite>();
    private List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne (mappedBy = "study")
    @Cascade (value = { CascadeType.ALL })
    public PlannedSchedule getPlannedSchedule() {
        return plannedSchedule;
    }

    public void setPlannedSchedule(PlannedSchedule plannedSchedule) {
        this.plannedSchedule = plannedSchedule;
        if (plannedSchedule != null && plannedSchedule.getStudy() != this) {
            plannedSchedule.setStudy(this);
        }
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
