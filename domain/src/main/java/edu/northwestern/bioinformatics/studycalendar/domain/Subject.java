package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
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
    private Gender gender;
    private String personId;
    private List<StudySubjectAssignment> assignments = new ArrayList<StudySubjectAssignment>();
    private List<SubjectProperty> properties = new ArrayList<SubjectProperty>();

    ////// LOGIC

    public void addAssignment(StudySubjectAssignment studySubjectAssignment) {
        getAssignments().add(studySubjectAssignment);
        studySubjectAssignment.setSubject(this);
    }

    @Transient
    public String getLastFirst() {
        StringBuilder name = new StringBuilder();
        boolean hasFirstName = true;
        if (getFirstName() == null || getFirstName().length() == 0) {
            hasFirstName = false;
        }
        boolean hasLastName = true;
        if (getLastName() == null || getLastName().length() == 0) {
            hasLastName = false;
        }
        if (hasLastName) {
            name.append(getLastName());
            if (hasFirstName) name.append(", ");
        }
        if (hasFirstName) {
            name.append(getFirstName());
        }

        if (name.length() > 0) {
            return name.toString();
        } else {
            return getPersonId();
        }
    }

    @Transient
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        boolean hasFirstName = true;
        if (getFirstName() == null || getFirstName().length() == 0) {
            hasFirstName = false;
        }
        boolean hasLastName = true;
        if (getLastName() == null || getLastName().length() == 0) {
            hasLastName = false;
        }

        if (hasFirstName) {
            name.append(getFirstName());
            if (hasLastName) name.append(' ');
        }
        if (hasLastName) {
            name.append(getLastName());
        }
        if (name.length()>0) {
            return name.toString();
        } else {
            return getPersonId();
        }
    }

    public String getProperty(String name) {
        for (SubjectProperty property : getProperties()) {
            if (name.equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }

    ////// BEAN ACCESSORS

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

    @Type(type = "gender")
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Column(name = "person_id", unique = true)
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

    @SuppressWarnings( { "UnusedDeclaration" }) // used by hibernate
    public void setAssignments(List<StudySubjectAssignment> assignments) {
        this.assignments = assignments;
    }

    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(
        name = "subject_properties",
        joinColumns = @JoinColumn(name = "subject_id")
    )
    @IndexColumn(name = "list_index")
    public List<SubjectProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<SubjectProperty> properties) {
        this.properties = properties;
    }

    public void replaceProperties(List<SubjectProperty> properties) {
        this.properties.clear();
        this.properties.addAll(properties);
    }

    ////// OBJECT METHODS

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Subject that = (Subject) o;

        if (dateOfBirth != null ? !dateOfBirth.equals(that.getDateOfBirth()) : that.getDateOfBirth() != null)
            return false;
        if (firstName != null ? !firstName.equals(that.getFirstName()) : that.getFirstName() != null)
            return false;
        if (gender != null ? !gender.equals(that.getGender()) : that.getGender() != null) return false;
        if (lastName != null ? !lastName.equals(that.getLastName()) : that.getLastName() != null) return false;
        if (personId != null ? !personId.equals(that.getPersonId()) : that.getPersonId() != null) return false;
        if (assignments != null ? !assignments.equals(that.getAssignments()) : that.getAssignments() != null)
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
}
