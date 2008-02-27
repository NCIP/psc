package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Entity
@Table
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
        @Parameter(name = "sequence", value = "seq_subjects_id")
                }
)
@Where(clause = "load_status > 0")
public class Subject extends AbstractMutableDomainObject {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String personId;
    private List<StudySubjectAssignment> assignments = new ArrayList<StudySubjectAssignment>();
    private LoadStatus loadStatus = LoadStatus.COMPLETE;

    // business methods

    // The subject identifier could be the Medical Record No based on the site

    public void addAssignment(StudySubjectAssignment studySubjectAssignment) {
        getAssignments().add(studySubjectAssignment);
        studySubjectAssignment.setSubject(this);
    }

    @Transient
    public String getLastFirst() {
        StringBuilder name = new StringBuilder();
        boolean hasFirstName = getFirstName() != null;
        if (getLastName() != null) {
            name.append(getLastName());
            if (hasFirstName) name.append(", ");
        }
        if (hasFirstName) {
            name.append(getFirstName());
        }
        return name.toString();
    }

    @Transient
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        boolean hasLastName = getLastName() != null;
        if (getFirstName() != null) {
            name.append(getFirstName());
            if (hasLastName) name.append(' ');
        }
        if (hasLastName) {
            name.append(getLastName());
        }
        return name.toString();
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

    // TODO: Create enum for gender
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Column(name = "person_id", unique = true, nullable = false)
    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @OneToMany(mappedBy = "subject")
    @OrderBy
    // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<StudySubjectAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<StudySubjectAssignment> assignments) {
        this.assignments = assignments;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Subject that = (Subject) o;

        if (dateOfBirth != null ? !dateOfBirth.equals(that.dateOfBirth) : that.dateOfBirth != null)
            return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null)
            return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;
        if (personId != null ? !personId.equals(that.personId) : that.personId != null) return false;
        if (assignments != null ? !assignments.equals(that.assignments) : that.assignments != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (firstName != null ? firstName.hashCode() : 0);
        result = 29 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 29 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 29 * result + (gender != null ? gender.hashCode() : 0);
        result = 29 * result + (personId != null ? personId.hashCode() : 0);
        return result;
    }

    @Enumerated(EnumType.ORDINAL)
    public LoadStatus getLoadStatus() {
        return loadStatus;
    }

    /**
     * Added for hibernate only..
     * This method will not change the load status...The load status will always be {LoadStatus.COMPLETE}.
     * @param loadStatus
     */
    public void setLoadStatus(final LoadStatus loadStatus) {

        //this.loadStatus = loadStatus;
    }

}
