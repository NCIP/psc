package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table(name = "sites")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {@Parameter(name = "sequence", value = "seq_sites_id")}
)
public class Site extends AbstractMutableDomainObject implements Named, Serializable, NaturallyKeyed {
    private String name;

    private List<StudySite> studySites = new ArrayList<StudySite>();

    private String assignedIdentifier;

    private List<BlackoutDate> blackoutDates = new ArrayList<BlackoutDate>();

    ////// LOGIC

    public boolean hasAssignments() {
        for (StudySite studySite : getStudySites()) {
            if (studySite.getStudySubjectAssignments().size() > 0) return true;
        }
        return false;
    }

    public void addStudySite(final StudySite studySite) {
        getStudySites().add(studySite);
        studySite.setSite(this);
    }

    public StudySite getStudySite(final Study study) {
        for (StudySite studySite : getStudySites()) {
            if (studySite.getStudy().equals(study)) {
                return studySite;
            }
        }
        return null;
    }

    @Transient
    public String getNaturalKey() {
        return getAssignedIdentifier();
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setStudySites(final List<StudySite> studySites) {
        this.studySites = studySites;
    }

    @OneToMany(mappedBy = "site")
    @OrderBy
    // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<StudySite> getStudySites() {
        return studySites;
    }

    public void setBlackoutDates(final List<BlackoutDate> blackoutDates) {
        this.blackoutDates = blackoutDates;
    }

    public String getAssignedIdentifier() {
        if (assignedIdentifier == null) {
            return getName();
        } else {
            return assignedIdentifier;
        }
    }

    public void setAssignedIdentifier(final String assignedIdentifier) {
        this.assignedIdentifier = assignedIdentifier;
    }

    @OneToMany(mappedBy = "site")
    @OrderBy
    // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<BlackoutDate> getBlackoutDates() {
        return blackoutDates;
    }

    @Transient
    public boolean checkIfHolidayExists(final BlackoutDate blackoutDate) {

        for (BlackoutDate existingBlackoutDate : this.getBlackoutDates()) {
            if (existingBlackoutDate.getId() != null && blackoutDate != null && existingBlackoutDate.getId().equals(blackoutDate.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public void removeHoliday(final BlackoutDate blackoutDate) {
        BlackoutDate holidayToRemove = null;
        for (BlackoutDate existingBlackoutDate : this.getBlackoutDates()) {
            if (existingBlackoutDate.getId() != null && existingBlackoutDate.getId().equals(blackoutDate.getId())) {
                holidayToRemove = existingBlackoutDate;
                break;
            }
        }

        if (holidayToRemove != null) {
            getBlackoutDates().remove(holidayToRemove);
        }

    }

    /**
     * Adds the holiday if holiday does not exists. Or updates the holiday if holiday already exists
     *
     * @param blackoutDate
     */
    @Transient
    public void addOrMergeExistingHoliday(final BlackoutDate blackoutDate) {

        BlackoutDate holidayToAddOrMerge = null;
        for (BlackoutDate existingBlackoutDate : this.getBlackoutDates()) {
            if (existingBlackoutDate.getId() != null && existingBlackoutDate.getId().equals(blackoutDate.getId())) {
                holidayToAddOrMerge = existingBlackoutDate;
                break;
            }
        }

        if (holidayToAddOrMerge != null) {
            holidayToAddOrMerge.mergeAnotherHoliday(blackoutDate);

        } else {
            getBlackoutDates().add(blackoutDate);

        }

    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Site site = (Site) o;

        if (name != null ? !name.equals(site.name) : site.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId())
            .append("; name=").append(getName())
            .append("; assignedIdentifier=").append(getAssignedIdentifier())
            .append(']').toString();
    }
}
