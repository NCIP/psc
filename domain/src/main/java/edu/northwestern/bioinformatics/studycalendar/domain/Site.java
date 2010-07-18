package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "sites")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {@Parameter(name = "sequence", value = "seq_sites_id")}
)
public class Site extends AbstractProvidableDomainObject implements Named, Serializable, NaturallyKeyed {
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

    @Transient
    public boolean isBlackoutDatePresent(final BlackoutDate blackoutDate) {
        for (BlackoutDate existingBlackoutDate : this.getBlackoutDates()) {
            if (existingBlackoutDate.getId() != null && blackoutDate != null && existingBlackoutDate.getId().equals(blackoutDate.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public void removeBlackoutDate(final BlackoutDate blackoutDate) {
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

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "site")
    @OrderBy // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<StudySite> getStudySites() {
        return studySites;
    }

    public void setStudySites(final List<StudySite> studySites) {
        this.studySites = studySites;
    }

    @OneToMany(mappedBy = "site")
    @OrderBy // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<BlackoutDate> getBlackoutDates() {
        return blackoutDates;
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

    ////// OBJECT METHODS

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        
        if (!(o instanceof Site)) return false;

        Site site = (Site) o;

        if (getAssignedIdentifier() != null ? !getAssignedIdentifier().equals(site.getAssignedIdentifier()) : site.getAssignedIdentifier()!= null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getAssignedIdentifier() != null ? getAssignedIdentifier().hashCode() : 0;
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
